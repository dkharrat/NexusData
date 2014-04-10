package com.github.dkharrat.nexusdata.core;

/**
 * Represents a save request to a persistence store, containing all the objects that have been changed, either by
 * insertion, deletion, or updating, and are to be reflected in the persistence store. This request is sent by the
 * {@link com.github.dkharrat.nexusdata.core.ObjectContext} when a save is performed.
 */
public class SaveChangesRequest implements PersistentStoreRequest {

    private final ChangedObjectsSet changedObjects;

    SaveChangesRequest(ChangedObjectsSet changedObjects) {
        this.changedObjects = changedObjects;
    }

    /**
     * Returns all the objects that have been changed.
     *
     * @return Returns all the objects that need to be inserted
     */
    public ChangedObjectsSet getChanges() {
        return changedObjects;
    }
}
