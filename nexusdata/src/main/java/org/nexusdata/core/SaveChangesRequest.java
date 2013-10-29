package org.nexusdata.core;

import java.util.Set;

/**
 * Represents a save request to a persistence store, containing all the objects that have been changed, either by
 * insertion, deletion, or updating, and are to be reflected in the persistence store. This request is sent by the
 * @{org.nexusdata.core.ObjectContext} when a save is performed.
 */
public class SaveChangesRequest implements PersistentStoreRequest {

    private final ChangedObjectsSet changedObjects;

    SaveChangesRequest(ChangedObjectsSet changedObjects) {
        this.changedObjects = changedObjects;
    }

    /**
     * Returns all the objects that need to be inserted into the persistence store
     *
     * @return Returns all the objects that need to be inserted
     */
    public Set<ManagedObject> getInsertedObjects() {
        return changedObjects.getInsertedObjects();
    }

    /**
     * Returns all the objects that need to be deleted from the persistence store
     *
     * @return Returns all the objects that need to be deleted
     */
    public Set<ManagedObject> getDeletedObjects() {
        return changedObjects.getDeletedObjects();
    }

    /**
     * Returns all the objects that need to be updated in the persistence store
     *
     * @return Returns all the objects that need to be updated
     */
    public Set<ManagedObject> getUpdatedObjects() {
        return changedObjects.getUpdatedObjects();
    }
}
