package org.nexusdata.core;

import java.util.HashSet;
import java.util.Set;


public class ObjectsChangedNotification extends ChangedObjectsSet {

    private final Set<ManagedObject> m_refreshedObjects;

    ObjectsChangedNotification() {
        this(new HashSet<ManagedObject>(), new HashSet<ManagedObject>(), new HashSet<ManagedObject>(), new HashSet<ManagedObject>());
    }

    ObjectsChangedNotification(
            Set<ManagedObject> insertedObjects,
            Set<ManagedObject> updatedObjects,
            Set<ManagedObject> deletedObjects,
            Set<ManagedObject> refreshedObjects) {
        super(insertedObjects, updatedObjects, deletedObjects);
        m_refreshedObjects = refreshedObjects;
    }

    ObjectsChangedNotification(
            ChangedObjectsSet changedObjects,
            Set<ManagedObject> refreshedObjects) {
        this(changedObjects.getInsertedObjects(), changedObjects.getUpdatedObjects(), changedObjects.getDeletedObjects(), refreshedObjects);
    }

    @Override
    void clear() {
        super.clear();
        m_refreshedObjects.clear();
    }

    public Set<ManagedObject> getRefreshedObjects() {
        return m_refreshedObjects;
    }

    void objectRefreshed(ManagedObject object) {
        m_refreshedObjects.add(object);
    }

    public boolean isRefreshed(ManagedObject object) {
        return m_refreshedObjects.contains(object);
    }
}
