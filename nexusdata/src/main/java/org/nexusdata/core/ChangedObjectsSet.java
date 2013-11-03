package org.nexusdata.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a set of objects that have changed in an {@link ObjectContext}. It is used by events sent out from
 * {@link ObjectContextNotifier}, persistence store save requests, etc.
 */
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
        if (!object.isInserted() || trackDeletionEvenIfNew) {
            deletedObjects.add(object);
        }
    }

    void clear() {
        insertedObjects.clear();
        deletedObjects.clear();
        updatedObjects.clear();
    }

    /**
     * Returns the set of objects that have been inserted into the ObjectContext.
     *
     * @return the set of objects that have been inserted
     */
    public Set<ManagedObject> getInsertedObjects() {
        return insertedObjects;
    }

    /**
     * Returns the set of objects that have been updated (have property changes).
     *
     * @return the set of objects that have been updated
     */
    public Set<ManagedObject> getUpdatedObjects() {
        return updatedObjects;
    }

    /**
     * Returns the set of objects that have been deleted from the ObjectContext.
     *
     * @return the set of objects that have been deleted
     */
    public Set<ManagedObject> getDeletedObjects() {
        return deletedObjects;
    }

    /**
     * Returns true if the specified object is marked as inserted by this change set
     *
     * @return true if the specified object is marked as inserted by this change set
     */
    public boolean isInserted(ManagedObject object) {
        return insertedObjects.contains(object);
    }

    /**
     * Returns true if the specified object is marked as updated by this change set
     *
     * @return true if the specified object is marked as updated by this change set
     */
    public boolean isUpdated(ManagedObject object) {
        return updatedObjects.contains(object);
    }

    /**
     * Returns true if the specified object is marked as deleted by this change set
     *
     * @return true if the specified object is marked as deleted by this change set
     */
    public boolean isDeleted(ManagedObject object) {
        return deletedObjects.contains(object);
    }

    /**
     * Returns true if this change set is non-empty (i.e. changes exist)
     *
     * @return true if this change set is non-empty
     */
    public boolean hasChanges() {
        return  !insertedObjects.isEmpty() ||
                !updatedObjects.isEmpty() ||
                !deletedObjects.isEmpty();
    }
}
