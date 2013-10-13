package org.nexusdata.core;

import java.util.HashSet;
import java.util.Set;


public class ChangedObjectsSet {

    private final Set<ManagedObject> insertedObjects;
    private final Set<ManagedObject> updatedObjects;
    private final Set<ManagedObject> deletedObjects;

    ChangedObjectsSet() {
        this(new HashSet<ManagedObject>(), new HashSet<ManagedObject>(), new HashSet<ManagedObject>());
    }

    ChangedObjectsSet(ChangedObjectsSet other) {
        this(new HashSet<ManagedObject>(other.getInsertedObjects()),
                new HashSet<ManagedObject>(other.getUpdatedObjects()),
                new HashSet<ManagedObject>(other.getDeletedObjects()));
    }

    ChangedObjectsSet(Set<ManagedObject> insertedObjects, Set<ManagedObject> updatedObjects, Set<ManagedObject> deletedObjects) {
        this.insertedObjects = insertedObjects;
        this.updatedObjects = updatedObjects;
        this.deletedObjects = deletedObjects;
    }

    void objectInserted(ManagedObject object) {
        if (!object.isDeleted()) {
            insertedObjects.add(object);
        }
        deletedObjects.remove(object);
    }

    void objectUpdated(ManagedObject object) {
        if (!insertedObjects.contains(object) && !deletedObjects.contains(object)) {
            updatedObjects.add(object);
        }
    }

    void objectDeleted(ManagedObject object, boolean trackDeletionEvenIfNew) {
        insertedObjects.remove(object);
        updatedObjects.remove(object);
        if (!object.isNew() || trackDeletionEvenIfNew) {
            deletedObjects.add(object);
        }
    }

    void clear() {
        insertedObjects.clear();
        deletedObjects.clear();
        updatedObjects.clear();
    }

    public Set<ManagedObject> getInsertedObjects() {
        return insertedObjects;
    }

    public Set<ManagedObject> getUpdatedObjects() {
        return updatedObjects;
    }

    public Set<ManagedObject> getDeletedObjects() {
        return deletedObjects;
    }

    public boolean isInserted(ManagedObject object) {
        return insertedObjects.contains(object);
    }

    public boolean isUpdated(ManagedObject object) {
        return updatedObjects.contains(object);
    }

    public boolean isDeleted(ManagedObject object) {
        return deletedObjects.contains(object);
    }

    public boolean hasChanges() {
        return  !insertedObjects.isEmpty() ||
                !updatedObjects.isEmpty() ||
                !deletedObjects.isEmpty();
    }
}
