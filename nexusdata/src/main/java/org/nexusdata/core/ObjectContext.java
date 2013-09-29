package org.nexusdata.core;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.nexusdata.metamodel.AttributeDescription;
import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.metamodel.PropertyDescription;
import org.nexusdata.predicate.Predicate;
import org.nexusdata.utils.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.nexusdata.metamodel.AttributeDescription;
import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.metamodel.PropertyDescription;
import org.nexusdata.metamodel.RelationshipDescription;
import org.nexusdata.predicate.Predicate;
import org.nexusdata.utils.ObjectUtil;

// TODO: ObjectContext changes
//  * unregister objects when they are no longer referenced
//  * Implement custom Exception classes to identify different error types
//  * Add support to query objects of super entity type

public class ObjectContext {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectContext.class);

    private final PersistentStoreCoordinator m_storeCoordinator;

    // TODO: use weak reference for storing objects
    private final Map<ObjectID, ManagedObject> m_objects = new HashMap<ObjectID, ManagedObject>();
    private final ChangedObjectsSet m_changedObjects = new ChangedObjectsSet();

    private final ObjectsChangedNotification m_objectsChangedSinceLastNotification = new ObjectsChangedNotification();

    private final static int NOTIFY_OBJECTS_CHANGED = 1;
    //TODO: abstract this away for multi-platform support (use Futures?)
    private final Handler m_messageHandler;

    public ObjectContext(PersistentStoreCoordinator storeCoordinator) {
        m_storeCoordinator = storeCoordinator;

        // If there is no event loop in the current thread, use the main thread's event loop
        Looper looper = Looper.myLooper();
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        m_messageHandler = new ObjectContextMessageHandler(looper, this);
    }

    public PersistentStoreCoordinator getPersistentStoreCoordinator() {
        return m_storeCoordinator;
    }

    public <T extends ManagedObject> FetchRequest.Builder<T> newFetchRequestBuilder(Class<T> entityType) {
        return FetchRequest.Builder.forEntity(m_storeCoordinator.getModel().getEntity(entityType));
    }

    @SuppressWarnings("unchecked")
    public <T extends ManagedObject> List<T> executeFetchOperation(FetchRequest<T> fetchRequest) {

        List<T> results = new ArrayList<T>();

        //FIXME: properly route the fetch to the right store
        PersistentStore store = getPersistentStoreCoordinator().getPersistentStores().get(0);

        results = store.executeFetchRequest(fetchRequest, this);

        if (fetchRequest.includesPendingChanges()) {
            for (ManagedObject object : m_changedObjects.getInsertedObjects()) {
                ObjectID objID = object.getID();
                if (objID.getType().isAssignableFrom(fetchRequest.getEntity().getType())) {
                    if (fetchRequest.getPredicate() == null || fetchRequest.getPredicate().evaluate(object)) {
                        results.add((T)object);
                    }
                }
            }

            //FIXME: also search from updated objects

            results.removeAll(getDeletedObjects());
        }

        return results;
    }

    public <T extends ManagedObject> List<T> findAll(Class<T> type) {
        return findAll(type, null);
    }

    public <T extends ManagedObject> List<T> findAll(Class<T> type, Predicate predicate) {
        EntityDescription<T> entity = m_storeCoordinator.getModel().getEntity(type);
        FetchRequest<T> fetchRequest = new FetchRequest<T>(entity);
        fetchRequest.setPredicate(predicate);
        return this.executeFetchOperation(fetchRequest);
    }

    @SuppressWarnings("unchecked")
    public <T extends ManagedObject> T objectWithID(ObjectID id) {
        T object = (T) m_objects.get(id);

        if (object == null) {
            object = ManagedObject.newObject(id);
            registerObject(object);
        }

        return object;
    }

    public ManagedObject objectWithID(URI objectIDUri) {
        ObjectID id = m_storeCoordinator.objectIDFromUri(objectIDUri);
        return objectWithID(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends ManagedObject> T getExistingObject(ObjectID id) {
        T object = (T) m_objects.get(id);

        if (object == null) {
            object = objectWithID(id);
            faultInObject(object);
        }

        return object;
    }

    public ManagedObject getExistingObject(URI objectIDUri) {
        ObjectID id = m_storeCoordinator.objectIDFromUri(objectIDUri);
        return getExistingObject(id);
    }

    public <T extends ManagedObject> T newObject(Class<T> type) {
        EntityDescription<T> entity = m_storeCoordinator.getModel().getEntity(type);
        ObjectID id = new ObjectID(null, entity, UUID.randomUUID());

        T object = ManagedObject.newObject(id);
        insert(object);
        object.init();

        return object;
    }

    void faultInObject(ManagedObject object) {
        //LOG.debug("Fulfilling fault on objectID: " + object.getID());

        PersistentStore store = object.getID().getPersistentStore();
        StoreCacheNode cacheNode = store.getObjectValues(object.getID(), this);

        if (cacheNode == null) {
            throw new RuntimeException("Could not find object " + object + " in persistent store");
        }

        for (PropertyDescription property : object.getEntity().getProperties()) {
            Object value = cacheNode.getProperty(property.getName());

            if (property.isRelationship()) {
                RelationshipDescription relationship = (RelationshipDescription)property;
                if (relationship.isToMany()) {
                    value = new FaultingSet(object, relationship, null);
                } else {
                    if (value != null) {
                        if (!(value instanceof ObjectID)) {
                            throw new IllegalStateException("Cache node value for to-one relationship '"+property.getName()+"' should reference an ObjectID (got " + value + ")");
                        }
                        value = objectWithID((ObjectID) value);
                    } else if (!cacheNode.hasProperty(property.getName())) {
                        // cache node does not explicitly have the to-one relationship, so let's retrieve it
                        faultInObjectRelationship(object, relationship);
                        continue;   // faulting relationship should have set the proper value
                    }
                }
            }

            //TODO: do we need to consider relationship consistency for to-one relationship values (i.e. update the other end of the relationship)?
            object.setValueDirectly(property, value);
        }
    }

    void faultInObjectRelationship(ManagedObject object, RelationshipDescription relationship) {
        LOG.debug("Fulfilling fault on relationship " + relationship.getName() + " for objectID: " + object.getID());
        PersistentStore store = object.getID().getPersistentStore();

        if (relationship.isToOne()) {
            ManagedObject value = null;
            ObjectID id = store.getToOneRelationshipValue(object.getID(), relationship, this);
            if (id != null) {
                value = objectWithID(id);
            }
            object.setValueDirectly(relationship, value);
        } else {    // to-many relationship
            FaultingSet<ManagedObject> objectsSet = (FaultingSet<ManagedObject>) object.getValueDirectly(relationship);

            Collection<ObjectID> ids = store.getToManyRelationshipValue(object.getID(), relationship, this);
            List<ManagedObject> objects = new ArrayList<ManagedObject>();
            for (ObjectID id : ids) {
                ManagedObject element = objectWithID(id);
                objects.add(element);
            }

            objectsSet.setObjects(objects);
        }
    }

    public Set<ManagedObject> getRegisteredObjects() {
        return new HashSet<ManagedObject>(m_objects.values());
    }

    private void registerObject(ManagedObject object) {

        PersistentStore store = object.getID().getPersistentStore();

        if (object.getID().getPersistentStore() != null && object.getID().getPersistentStore() != store) {
            throw new RuntimeException("Object's persistence store is not reachable from this context");
        }

        object.setManagedObjectContext(this);
        m_objects.put(object.getID(), object);

        //FIXME: temporary object won't have a store assign yet
        /* List<ObjectID<?>> objectIDs = new ArrayList<ObjectID<?>>();
        objectIDs.add(object.getID());
        store.contextRegisteredObjectIDs(objectIDs); */
    }

    private void unregisterObject(ManagedObject object) {
        object.setManagedObjectContext(null);
        m_objects.remove(object.getID());

        PersistentStore store = object.getID().getPersistentStore();

        //FIXME: temporary object won't have a store assign yet
        /*List<ObjectID<?>> objectIDs = new ArrayList<ObjectID<?>>();
        objectIDs.add(object.getID());
        store.contextUnregisteredObjectIDs(objectIDs);*/
    }

    private void registerObjects(Collection<ManagedObject> objects) {
        if (!objects.isEmpty()) {

            List<ObjectID> objectIDs = new ArrayList<ObjectID>(objects.size());
            for (ManagedObject object : objects) {
                object.setManagedObjectContext(this);
                m_objects.put(object.getID(), object);
                objectIDs.add(object.getID());
            }

            //TODO: need to fix
            /*
            PersistentStore store = object.getID().getPersistentStore();
            store.contextRegisteredObjectIDs(objectIDs);
            */
        }
    }

    private void unregisterObjects(Collection<ManagedObject> objects) {
        if (!objects.isEmpty()) {

            List<ObjectID> objectIDs = new ArrayList<ObjectID>(objects.size());
            for (ManagedObject object : objects) {
                object.setManagedObjectContext(null);
                m_objects.remove(object.getID());
                objectIDs.add(object.getID());
            }

            //TODO: need to fix
            /*
            PersistentStore store = object.getID().getPersistentStore();
            store.contextUnregisteredObjectIDs(objectIDs);
            */
        }
    }

    private void unregisterAllObjects() {
        if (!m_objects.isEmpty()) {
            Collection<ObjectID> objectIDs = m_objects.keySet();
            for (ManagedObject object : m_objects.values()) {
                object.setManagedObjectContext(null);
            }
            m_objects.clear();

            // TODO: need to fix
            /*
            PersistentStore store = object.getID().getPersistentStore();
                store.contextUnregisteredObjectIDs(objectIDs);
            */
        }
    }

    boolean isCompatibleWithContext(ObjectContext otherContext) {
        return m_storeCoordinator == otherContext.getPersistentStoreCoordinator();
    }

    private void sendObjectsChangedNotification() {
        if (ObjectContextNotifier.hasListeners(this) && !m_messageHandler.hasMessages(NOTIFY_OBJECTS_CHANGED)) {
            m_messageHandler.sendEmptyMessage(NOTIFY_OBJECTS_CHANGED);
        }
    }

    public void insert(ManagedObject object) {
        if (object.getEntity() == null || object.getID() == null) {
            throw new RuntimeException("Managed object " + object + " must be created through a context");
        }

        // check that the object is not associated with another context
        if (object.getObjectContext() != this && object.getObjectContext() != null) {
            throw new IllegalStateException("Object already associated with another context");
        }

        m_changedObjects.objectInserted(object);
        registerObject(object);

        m_objectsChangedSinceLastNotification.objectInserted(object);
        sendObjectsChangedNotification();
    }

    void markObjectAsUpdated(ManagedObject object) {
        m_changedObjects.objectUpdated(object);

        if (m_changedObjects.isUpdated(object)) {
            m_objectsChangedSinceLastNotification.objectUpdated(object);
            sendObjectsChangedNotification();
        }
    }

    public void delete(ManagedObject object) {
        m_changedObjects.objectDeleted(object, false);
        if (!object.isNew()) {
            registerObject(object);
        } else {
            unregisterObject(object);
        }

        m_objectsChangedSinceLastNotification.objectDeleted(object, true);
        sendObjectsChangedNotification();
    }

    public void markObjectAsRefreshed(ManagedObject object) {
        m_objectsChangedSinceLastNotification.objectRefreshed(object);
        sendObjectsChangedNotification();
    }

    private void updateRegisteredObjectID(ManagedObject object, ObjectID newID) {
        m_objects.remove(object.getID());
        object.setID(newID);
        m_objects.put(newID, object);
    }

    public <T extends ManagedObject> void obtainPermanentIDsForObjects(Collection<T> objects) {
        //FIXME: properly route the save to the right store for each object
        PersistentStore store = getPersistentStoreCoordinator().getPersistentStores().get(0);

        List<ManagedObject> objectsList = new ArrayList<ManagedObject>(objects.size());
        for (ManagedObject object : objects) {
            if (object.getID().isTemporary()) {
                objectsList.add(object);
            }
        }

        if (!objectsList.isEmpty()) {
            List<ObjectID> ids = store.getPermanentIDsForObjects(objectsList);
            for (int i=0; i<objectsList.size(); i++) {
                ObjectID id = ids.get(i);
                ManagedObject o = objectsList.get(i);
                updateRegisteredObjectID(o, id);
            }
        }
    }

    public void save() {
        if (!m_changedObjects.hasChanges()) {
            return;
        }

        ObjectContextNotifier.notifyListenersOfWillSave(this);

        //FIXME: properly route the save to the right store for each object
        PersistentStore store = getPersistentStoreCoordinator().getPersistentStores().get(0);

        // null-out to-one relationship references of deleted objects to ensure nothing references them
        for (ManagedObject object : m_changedObjects.getDeletedObjects()) {
            for (RelationshipDescription relationship : object.getEntity().getRelationships()) {
                if (relationship.isToOne()) {
                    object.setValue(relationship.getName(), null);
                }
            }
        }

        obtainPermanentIDsForObjects(m_changedObjects.getInsertedObjects());

        SaveChangesRequest request = new SaveChangesRequest(m_changedObjects);
        store.executeSaveRequest(request, this);

        ObjectContextNotifier.notifyListenersOfDidSave(this, new ChangedObjectsSet(m_changedObjects));

        unregisterObjects(m_changedObjects.getDeletedObjects());

        m_changedObjects.clear();
    }

    public boolean hasChanges() {
        return m_changedObjects.hasChanges();
    }

    public Set<ManagedObject> getInsertedObjects() {
        return Collections.unmodifiableSet(m_changedObjects.getInsertedObjects());
    }

    public Set<ManagedObject> getUpdatedObjects() {
        return Collections.unmodifiableSet(m_changedObjects.getUpdatedObjects());
    }

    public Set<ManagedObject> getDeletedObjects() {
        return Collections.unmodifiableSet(m_changedObjects.getDeletedObjects());
    }

    public void reset() {
        m_changedObjects.clear();
        unregisterAllObjects();
    }

    public void mergeChangesFromSaveNotification(ChangedObjectsSet changedObjects) {
        //TODO: this should run in the same thread that was used to create the context (use Handler to do that)

        for (ManagedObject o : changedObjects.getInsertedObjects()) {
            ManagedObject object = objectWithID(o.getID());    // registers new object
            m_objectsChangedSinceLastNotification.objectInserted(object);
        }

        for (ManagedObject otherObject : changedObjects.getUpdatedObjects()) {
            ManagedObject object = objectWithID(otherObject.getID());
            if (!object.isFault()) {
                boolean objectChanged = false;
                for (PropertyDescription property : object.getEntity().getProperties()) {
                    Object otherValue = otherObject.getValueDirectly(property);
                    Object value = otherValue;
                    if (property.isRelationship()) {
                        RelationshipDescription relationship = (RelationshipDescription) property;
                        if (relationship.isToOne()) {
                            if (otherValue != null) {
                                value = objectWithID(((ManagedObject)otherValue).getID());
                            }
                        } else {
                            value = ((FaultingSet<?>)otherValue).getObjectsInContext(this);
                        }
                    }

                    Object oldValue = object.getValueDirectly(property);
                    if (!ObjectUtil.objectsEqual(oldValue, value)) {
                        object.setValueDirectly(property, value);
                        objectChanged = true;
                    }
                }

                if (objectChanged) {
                    m_objectsChangedSinceLastNotification.objectUpdated(object);
                }
            }
        }

        for (ManagedObject o : changedObjects.getDeletedObjects()) {
            ManagedObject object = objectWithID(o.getID());
            delete(object);
            m_objectsChangedSinceLastNotification.objectDeleted(object, false);
        }

        // since we've merged from another save operation, ensure merged objects are not marked as changed
        m_changedObjects.getInsertedObjects().removeAll(changedObjects.getInsertedObjects());
        m_changedObjects.getUpdatedObjects().removeAll(changedObjects.getUpdatedObjects());
        m_changedObjects.getDeletedObjects().removeAll(changedObjects.getDeletedObjects());

        sendObjectsChangedNotification();
    }

    /**
     * Clones the specified object. The cloned object is then inserted into this context.
     * <p>
     * Note: Only the object attributes are copied over; relationships are ignored
     *
     * @param otherObject   The object to copy from.
     * @return the cloned and newly inserted object
     */
    public ManagedObject cloneObject(ManagedObject otherObject) {
        ManagedObject object = newObject(otherObject.getEntity().getType());

        for (AttributeDescription attribute : object.getEntity().getAttributes()) {
            object.setValue(attribute.getName(), otherObject.getValue(attribute.getName()));
        }

        return object;
    }

    public boolean isInserted(ManagedObject object) {
        return m_changedObjects.isInserted(object);
    }

    public boolean isUpdated(ManagedObject object) {
        return m_changedObjects.isUpdated(object);
    }

    public boolean isDeleted(ManagedObject object) {
        return m_changedObjects.isDeleted(object);
    }

    private static class ObjectContextMessageHandler extends Handler {

        private final WeakReference<ObjectContext> contextReference;

        ObjectContextMessageHandler(Looper looper, ObjectContext context) {
            super(looper);
            contextReference = new WeakReference<ObjectContext>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            ObjectContext context = contextReference.get();

            switch (msg.what) {
                case NOTIFY_OBJECTS_CHANGED: {
                    if (context != null) {
                        ObjectContextNotifier.notifyListenersOfObjectsDidChange(context, context.m_objectsChangedSinceLastNotification);
                        context.m_objectsChangedSinceLastNotification.clear();
                    }
                    break;
                }
            }
        }
    };
}
