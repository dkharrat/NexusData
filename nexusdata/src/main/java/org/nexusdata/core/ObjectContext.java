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

import org.nexusdata.metamodel.RelationshipDescription;

// TODO: unregister objects when they are no longer referenced
// TODO: Implement custom Exception classes to identify different error types
// TODO: Add support to query objects of super entity type
// TODO: Check for null for required properties

/**
 * An Object Context keeps track and manages a collection of objects used by an application. A context represents a
 * "scratchpad" where changes to objects can be made. Through a context, an object can be fetched from a persistence
 * store. Changes can be made to those objects. Also, new objects can be inserted, or existing objects can be deleted.
 * Any such changes can be either committed to the persistence store or discarded if they are not needed anymore. Every
 * managed object is registered with an ObjectContext. You can also have multiple object contexts. An object can exist
 * in multiple object contexts. However, each context will maintain its own copy of the object. This means, an object
 * can be edited in more than one context simultaneously. Note that, conflict resolution is currently not supported. So,
 * if two contexts track an object, each having different changes to the object, then saving the second context after
 * the first one is saved, will override any changes from the first.
 */
public class ObjectContext {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectContext.class);

    private final PersistentStoreCoordinator storeCoordinator;

    // TODO: use weak reference for storing objects
    private final Map<ObjectID, ManagedObject> objects = new HashMap<ObjectID, ManagedObject>();
    private final ChangedObjectsSet changedObjects = new ChangedObjectsSet();

    private final ObjectsChangedNotification objectsChangedSinceLastNotification = new ObjectsChangedNotification();

    private final static int NOTIFY_OBJECTS_CHANGED = 1;
    //TODO: abstract this away for multi-platform support (use Futures?)
    private final Handler messageHandler;

    /**
     * Creates a new ObjectContext instance that is associated with a persistence store coordinator.
     *
     * @param storeCoordinator the associated PersistenceStoreCoordinator that will be used to retrieve objects and
     *                         save objects to
     */
    public ObjectContext(PersistentStoreCoordinator storeCoordinator) {
        this.storeCoordinator = storeCoordinator;

        // If there is no event loop in the current thread, use the main thread's event loop
        Looper looper = Looper.myLooper();
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        messageHandler = new ObjectContextMessageHandler(looper, this);
    }

    /**
     * @return  The associated persistent store coordinator
     */
    public PersistentStoreCoordinator getPersistentStoreCoordinator() {
        return storeCoordinator;
    }

    /**
     * Returns a fetch request builder for the specified entity type, which can be used to build upon more constraints
     * if desired.
     *
     * @param entityType    the type of the entity to be fetched
     * @param <T>           parameterized type of the entity
     * @return              a fetch request builder for the specified entity type
     */
    public <T extends ManagedObject> FetchRequest.Builder<T> newFetchRequestBuilder(Class<T> entityType) {
        return FetchRequest.Builder.forEntity(storeCoordinator.getModel().getEntity(entityType));
    }

    /**
     * Fetches all objects that match the specified criteria from the persistence store coordinator. If the fetch
     * request does not contain a predicate, all objects for the specified entity will be returned. Returned objects
     * will maintain whatever state they are in before the fetch operation (i.e. objects will have the changes that
     * have been made within the context, if any, and not the state from the persistence store). Objects pending
     * deletion in the context will not be included in the returned results.
     *
     * @param fetchRequest      the fetch request that specifies the criteria
     * @param <T>               parameterized type of the entity to be fetched
     * @return                  a list of objects that match the criteria
     */
    @SuppressWarnings("unchecked")
    public <T extends ManagedObject> List<T> executeFetchOperation(FetchRequest<T> fetchRequest) {

        List<T> results = new ArrayList<T>();

        //FIXME: properly route the fetch to the right store
        PersistentStore store = getPersistentStoreCoordinator().getPersistentStores().get(0);

        results = store.executeFetchRequest(fetchRequest, this);

        if (fetchRequest.includesPendingChanges()) {
            for (ManagedObject object : changedObjects.getInsertedObjects()) {
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

    /**
     * Returns all objects of the specified entity type from the persistent store.
     *
     * @param type  The class type of the entity to retrieve
     * @param <T>   parameterized type of the entity to be fetched
     * @return      a list of all the objects that match the specified entity type
     */
    public <T extends ManagedObject> List<T> findAll(Class<T> type) {
        return findAll(type, null);
    }

    /**
     * Returns all objects of the specified entity type from the persistent store that match a predicate.
     *
     * @param type      The class type of the entity to retrieve
     * @param <T>       parameterized type of the entity to be fetched
     * @param predicate The predicate to test against
     * @return          a list of all the objects that match the specified entity type and predicate
     */
    public <T extends ManagedObject> List<T> findAll(Class<T> type, Predicate predicate) {
        EntityDescription<T> entity = storeCoordinator.getModel().getEntity(type);
        FetchRequest<T> fetchRequest = new FetchRequest<T>(entity);
        fetchRequest.setPredicate(predicate);
        return this.executeFetchOperation(fetchRequest);
    }

    /**
     * Returns the object that has the specified ID. If the object is not registered with the context, a new instance
     * will be created and returned as a fault. It is assumed that the object exists in the persistence store. If not,
     * an exception will be thrown when a fault is triggered (e.g. by accessing any property in the object).
     *
     * @param id    The object's ID
     * @return      The object with the specified ID
     */
    public ManagedObject objectWithID(ObjectID id) {
        ManagedObject object = objects.get(id);

        if (object == null) {
            object = ManagedObject.newObject(id);
            registerObject(object);
        }

        return object;
    }

    /**
     * Similar to {@link ObjectContext#objectWithID(ObjectID)} except this method uses the URI representation for
     * the ID.
     *
     * @param objectIDUri   The URI representation of the object ID
     * @return              The object with the specified ID
     */
    public ManagedObject objectWithID(URI objectIDUri) {
        ObjectID id = storeCoordinator.objectIDFromUri(objectIDUri);
        return objectWithID(id);
    }

    /**
     * Returns the object that has the specified ID. If the object is not registered with the context, it is fetched
     * from the persistence store and faulted into the context. An exception will be thrown if the object does not
     * exist.
     *
     * @param id    The object's ID
     * @return      The object with the specified ID
     */
    public ManagedObject getExistingObject(ObjectID id) {
        ManagedObject object = objects.get(id);

        if (object == null) {
            object = objectWithID(id);
        }

        if (object.isFault()) {
            faultInObject(object);
        }

        return object;
    }

    /**
     * Similar to {@link ObjectContext#getExistingObject(ObjectID)} except this method uses the URI representation for
     * the ID.
     *
     * @param objectIDUri   The URI representation of the object ID
     * @return              The object with the specified ID
     */
    public ManagedObject getExistingObject(URI objectIDUri) {
        ObjectID id = storeCoordinator.objectIDFromUri(objectIDUri);
        return getExistingObject(id);
    }

    /**
     * Creates a new object of the specified type. The object will automatically be inserted into this context, to be
     * saved to the persistence store the next time the context is saved. Therefore, it is not necessary to call
     * {@link #insert(ManagedObject)} for the object.
     *
     * @param type  the class type of the entity to create
     * @return      a new object instance
     */
    public <T extends ManagedObject> T newObject(Class<T> type) {
        EntityDescription<T> entity = storeCoordinator.getModel().getEntity(type);
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
                    value = new FaultingSet<ManagedObject>(object, relationship, null);
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
            @SuppressWarnings("unchecked")
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

    /**
     * Returns the objects that are registered with this context.
     *
     * @return the set of objects that are registered with this context
     */
    public Set<ManagedObject> getRegisteredObjects() {
        return new HashSet<ManagedObject>(objects.values());
    }

    private void registerObject(ManagedObject object) {

        PersistentStore store = object.getID().getPersistentStore();

        if (object.getID().getPersistentStore() != null && object.getID().getPersistentStore() != store) {
            throw new RuntimeException("Object's persistence store is not reachable from this context");
        }

        object.setManagedObjectContext(this);
        objects.put(object.getID(), object);

        //FIXME: temporary object won't have a store assign yet
        /* List<ObjectID<?>> objectIDs = new ArrayList<ObjectID<?>>();
        objectIDs.add(object.getID());
        store.contextRegisteredObjectIDs(objectIDs); */
    }

    private void unregisterObject(ManagedObject object) {
        object.setManagedObjectContext(null);
        objects.remove(object.getID());

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
                this.objects.put(object.getID(), object);
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
                this.objects.remove(object.getID());
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
        if (!objects.isEmpty()) {
            Collection<ObjectID> objectIDs = objects.keySet();
            for (ManagedObject object : objects.values()) {
                object.setManagedObjectContext(null);
            }
            objects.clear();

            // TODO: need to fix
            /*
            PersistentStore store = object.getID().getPersistentStore();
                store.contextUnregisteredObjectIDs(objectIDs);
            */
        }
    }

    boolean isCompatibleWithContext(ObjectContext otherContext) {
        return storeCoordinator == otherContext.getPersistentStoreCoordinator();
    }

    private void sendObjectsChangedNotification() {
        if (ObjectContextNotifier.hasListeners(this) && !messageHandler.hasMessages(NOTIFY_OBJECTS_CHANGED)) {
            messageHandler.sendEmptyMessage(NOTIFY_OBJECTS_CHANGED);
        }
    }

    /**
     * Specifies that the object is to be inserted into the persistence store the next time the context is saved.
     * If the object was already inserted into this context, the operation is ignored.
     *
     * @param object    the object to be inserted
     * @throws IllegalArgumentException     if the object is already associated with another context, or if the object
     *                                      was not created from a context
     */
    public void insert(ManagedObject object) {
        if (object.getEntity() == null || object.getID() == null) {
            throw new IllegalStateException("Managed object " + object + " must be created through a context");
        }

        // check that the object is not associated with another context
        if (object.getObjectContext() != this && object.getObjectContext() != null) {
            throw new IllegalStateException("Object already associated with another context");
        }

        changedObjects.objectInserted(object);
        registerObject(object);

        objectsChangedSinceLastNotification.objectInserted(object);
        sendObjectsChangedNotification();
    }

    void markObjectAsUpdated(ManagedObject object) {
        changedObjects.objectUpdated(object);

        if (changedObjects.isUpdated(object)) {
            objectsChangedSinceLastNotification.objectUpdated(object);
            sendObjectsChangedNotification();
        }
    }

    /**
     * Specifies that the object is to be removed from its persistence store the next time the context is
     * saved. If the object was already marked for deletion in this context, the operation is ignored. If the object
     * has not been saved to the persistence store yet, the object is simply deleted from the context.
     *
     * @param object    the object to be inserted
     */
    public void delete(ManagedObject object) {
        changedObjects.objectDeleted(object, false);
        if (object.isInserted()) {
            unregisterObject(object);
        } else {
            registerObject(object);
        }

        objectsChangedSinceLastNotification.objectDeleted(object, true);
        sendObjectsChangedNotification();
    }

    void markObjectAsRefreshed(ManagedObject object) {
        objectsChangedSinceLastNotification.objectRefreshed(object);
        sendObjectsChangedNotification();
    }

    private void updateRegisteredObjectID(ManagedObject object, ObjectID newID) {
        objects.remove(object.getID());
        object.setID(newID);
        objects.put(newID, object);
    }

    /**
     * Assigns permanent IDs for the specified objects. Objects that already have a permanent ID are ignored. A
     * permanent ID is used to uniquely identify an object in the persistence store. Usually, newly created objects
     * will have temporary IDs. It is not necessary to call this method directly on newly created objects, as
     * permanent IDs will be obtained for them before they are persisted to the store.
     *
     * @param objects   The list of objects
     */
    public void obtainPermanentIDsForObjects(Collection<ManagedObject> objects) {
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

    /**
     * Commits all unsaved changes of the registered objects to their associated persistence store.
     */
    public void save() {
        if (!changedObjects.hasChanges()) {
            return;
        }

        ObjectContextNotifier.notifyListenersOfWillSave(this);

        //FIXME: properly route the save to the right store for each object
        PersistentStore store = getPersistentStoreCoordinator().getPersistentStores().get(0);

        // null-out to-one relationship references of deleted objects to ensure nothing references them
        for (ManagedObject object : changedObjects.getDeletedObjects()) {
            for (RelationshipDescription relationship : object.getEntity().getRelationships()) {
                if (relationship.isToOne()) {
                    object.setValue(relationship.getName(), null);
                }
            }
        }

        obtainPermanentIDsForObjects(changedObjects.getInsertedObjects());

        SaveChangesRequest request = new SaveChangesRequest(changedObjects);
        store.executeSaveRequest(request, this);

        ObjectContextNotifier.notifyListenersOfDidSave(this, new ChangedObjectsSet(changedObjects));

        unregisterObjects(changedObjects.getDeletedObjects());

        changedObjects.clear();
    }

    /**
     * Indicates whether there are any unsaved changes to any object in this context.
     *
     * @return true if there are unsaved changes, or false otherwise.
     */
    public boolean hasChanges() {
        return changedObjects.hasChanges();
    }

    /**
     * Returns the set of objects that are pending insertion to the persistence store.
     *
     * @return the set of objects that are pending insertion to the persistence store
     */
    public Set<ManagedObject> getInsertedObjects() {
        return Collections.unmodifiableSet(changedObjects.getInsertedObjects());
    }

    /**
     * Returns the set of objects that have unsaved changes and haven't been persisted yet.
     *
     * @return the set of objects that have unsaved changes
     */
    public Set<ManagedObject> getUpdatedObjects() {
        return Collections.unmodifiableSet(changedObjects.getUpdatedObjects());
    }

    /**
     * Returns the set of objects that are pending deletion from the persistence store.
     *
     * @return the set of objects that are pending deletion from the persistence store
     */
    public Set<ManagedObject> getDeletedObjects() {
        return Collections.unmodifiableSet(changedObjects.getDeletedObjects());
    }

    /**
     * Discards all the unsaved changes made to the objects in this context, including objects that are pending
     * insertion or deletion.
     */
    public void reset() {
        changedObjects.clear();
        unregisterAllObjects();
    }

    /**
     * Merges all the changes that been made through another context. This is done by refreshing any objects that have
     * been updated outside of this context, faulting-in newly inserted objects, and removing deleted objects.
     *
     * @param changedObjects    the <code>ChangedObjectsSet</code> that was received from a save notification
     */
    public void mergeChangesFromSaveNotification(ChangedObjectsSet changedObjects) {
        //TODO: this should run in the same thread that was used to create the context (use Handler to do that)

        for (ManagedObject o : changedObjects.getInsertedObjects()) {
            ManagedObject object = objectWithID(o.getID());    // registers new object
            objectsChangedSinceLastNotification.objectInserted(object);
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
                    objectsChangedSinceLastNotification.objectUpdated(object);
                }
            }
        }

        for (ManagedObject o : changedObjects.getDeletedObjects()) {
            ManagedObject object = objectWithID(o.getID());
            unregisterObject(object);
            objectsChangedSinceLastNotification.objectDeleted(object, false);
        }

        // since we've merged from another save operation, ensure merged objects are not marked as changed
        this.changedObjects.getInsertedObjects().removeAll(changedObjects.getInsertedObjects());
        this.changedObjects.getUpdatedObjects().removeAll(changedObjects.getUpdatedObjects());
        this.changedObjects.getDeletedObjects().removeAll(changedObjects.getDeletedObjects());

        sendObjectsChangedNotification();
    }

    /**
     * Clones the specified object. The cloned object is then inserted into this context.
     * <p>
     * Note: Only the object attributes are copied over; relationships are skipped.
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

    boolean isInserted(ManagedObject object) {
        return changedObjects.isInserted(object);
    }

    boolean isUpdated(ManagedObject object) {
        return changedObjects.isUpdated(object);
    }

    boolean isDeleted(ManagedObject object) {
        return changedObjects.isDeleted(object);
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
                        ObjectContextNotifier.notifyListenersOfObjectsDidChange(context, context.objectsChangedSinceLastNotification);
                        context.objectsChangedSinceLastNotification.clear();
                    }
                    break;
                }
            }
        }
    };
}
