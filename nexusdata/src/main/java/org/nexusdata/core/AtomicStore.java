package org.nexusdata.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nexusdata.metamodel.PropertyDescription;
import org.nexusdata.metamodel.RelationshipDescription;
import org.nexusdata.utils.ObjectUtil;


public abstract class AtomicStore extends PersistentStore {

    private final Map<ObjectID, StoreCacheNode> idsToCacheNodes = new HashMap<ObjectID, StoreCacheNode>();

    public AtomicStore(File location) {
        super(location);
    }

    public abstract void load();
    public abstract void save();
    public abstract Object createReferenceObjectForManagedObject(ManagedObject object);

    @Override
    protected void loadMetadata() {
        // TODO Auto-generated method stub
    }

    Set<StoreCacheNode> getCacheNodes() {
        return new HashSet<StoreCacheNode>(idsToCacheNodes.values());
    }

    protected void addCacheNode(StoreCacheNode cacheNode) {
        idsToCacheNodes.put(cacheNode.getID(), cacheNode);
    }

    protected void removeCacheNode(StoreCacheNode cacheNode) {
        idsToCacheNodes.remove(cacheNode.getID());
    }

    protected void updateCacheNode(StoreCacheNode cacheNode, ManagedObject object) {
        for(PropertyDescription property : object.getEntity().getProperties()) {
            Object value = object.getValue(property.getName());
            if (property.isRelationship()) {
                RelationshipDescription relationship = (RelationshipDescription) property;
                if (relationship.isToOne()) {
                    ManagedObject relatedObject = (ManagedObject) value;
                    if (relatedObject != null) {
                        cacheNode.setProperty(relationship.getName(), relatedObject.getID());
                    }
                } else {
                    FaultingSet<?> relatedObjects = (FaultingSet<?>) value;
                    cacheNode.setProperty(relationship.getName(), relatedObjects.getObjectIDs());
                }
            } else {
                cacheNode.setProperty(property.getName(), value);
            }
        }
    }

    StoreCacheNode createCacheNode(ManagedObject object) {
        ObjectID id = object.getID();
        StoreCacheNode cacheNode = new StoreCacheNode(id);
        updateCacheNode(cacheNode, object);

        return cacheNode;
    }

    private StoreCacheNode getCacheNode(ObjectID objectID) {
        return idsToCacheNodes.get(objectID);
    }

    @Override
    List<ObjectID> getPermanentIDsForObjects(List<ManagedObject> objects) {
        List<ObjectID> objectIDs = new ArrayList<ObjectID>();
        for (ManagedObject object : objects) {
            ObjectID id;

            Object refObject = createReferenceObjectForManagedObject(object);
            id = createObjectID(object.getEntity(), refObject);

            objectIDs.add(id);
        }

        return objectIDs;
    }

    @Override
    StoreCacheNode getObjectValues(ObjectID objectID, ObjectContext context) {
        return getCacheNode(objectID);
    }

    @Override
    ObjectID getToOneRelationshipValue(
            ObjectID objectID,
            RelationshipDescription relationship,
            ObjectContext context) {

        StoreCacheNode cacheNode = getCacheNode(objectID);

        return (ObjectID) cacheNode.getProperty(relationship.getName());
    }

    @Override
    Collection<ObjectID> getToManyRelationshipValue(
            ObjectID objectID,
            RelationshipDescription relationship,
            ObjectContext context) {

        StoreCacheNode cacheNode = getCacheNode(objectID);
        @SuppressWarnings("unchecked")
        Set<ObjectID> relatedObjectIDs = (Set<ObjectID>) cacheNode.getProperty(relationship.getName());

        return relatedObjectIDs;
    }

    private <T extends ManagedObject> void sort(final List<T> list, final List<SortDescriptor> sortDescriptors) {
        Collections.sort(list, new Comparator<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public int compare(T lhs, T rhs) {
                int result = 0;
                for (SortDescriptor sortDesc : sortDescriptors) {
                    Object lhsValue = lhs.getValue(sortDesc.getAttributeName());
                    Object rhsValue = rhs.getValue(sortDesc.getAttributeName());

                    if (lhsValue == null && rhsValue == null) {
                        return 0;
                    } else if (lhsValue == null || rhsValue == null) {
                        return -1;
                    } else {
                        @SuppressWarnings("rawtypes")
                        Comparable lhsComparable = ObjectUtil.toComparable(lhsValue);
                        @SuppressWarnings("rawtypes")
                        Comparable rhsComparable = ObjectUtil.toComparable(rhsValue);
                        result = lhsComparable.compareTo(rhsComparable);

                        if (result == 0) {
                            continue;
                        }

                        result = sortDesc.isAscending() ? result : -result;
                    }
                }
                return result;
            }
        });
    }

    @Override
    <T extends ManagedObject> List<T> executeFetchRequest(final FetchRequest<T> request, final ObjectContext context) {
        List<T> results = new ArrayList<T>();

        for (StoreCacheNode cacheNode : getCacheNodes()) {
            ObjectID objID = cacheNode.getID();
            @SuppressWarnings("unchecked")
            T obj = (T)context.getExistingObject(objID);

            if (objID.getType().isAssignableFrom(request.getEntity().getType())) {
                if (request.getPredicate() == null || request.getPredicate().evaluate(obj)) {
                    results.add(obj);
                }
            }
        }

        if (request.hasSortDescriptors()) {
            sort(results, request.getSortDescriptors());
        }

        if (request.getLimit() < Integer.MAX_VALUE && request.getLimit() < results.size()) {
            results = results.subList(0, request.getLimit());
        }

        return results;
    }

    @Override
    void executeSaveRequest(SaveChangesRequest request, ObjectContext context) {
        for (ManagedObject object : request.getInsertedObjects()) {
            StoreCacheNode cacheNode = createCacheNode(object);
            addCacheNode(cacheNode);
        }

        for (ManagedObject object : request.getDeletedObjects()) {
            if (!object.isInserted()) {
                StoreCacheNode cacheNode = getObjectValues(object.getID(), context);
                removeCacheNode(cacheNode);
                object.setManagedObjectContext(null);
            }
        }

        for (ManagedObject object : request.getUpdatedObjects()) {
            StoreCacheNode cacheNode = getObjectValues(object.getID(), context);
            updateCacheNode(cacheNode, object);
        }

        save();
    }
}