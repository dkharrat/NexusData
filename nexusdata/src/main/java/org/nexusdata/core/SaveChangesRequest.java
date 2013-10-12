package org.nexusdata.core;

import java.util.Set;


public class SaveChangesRequest extends PersistentStoreRequest {

    private final ChangedObjectsSet changedObjects;

    SaveChangesRequest(ChangedObjectsSet changedObjects) {
        this.changedObjects = changedObjects;
    }

    public Set<ManagedObject> getInsertedObjects() {
        return changedObjects.getInsertedObjects();
    }

    public Set<ManagedObject> getDeletedObjects() {
        return changedObjects.getDeletedObjects();
    }

    public Set<ManagedObject> getUpdatedObjects() {
        return changedObjects.getUpdatedObjects();
    }
}
