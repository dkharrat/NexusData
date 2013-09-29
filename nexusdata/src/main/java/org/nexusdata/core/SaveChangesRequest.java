package org.nexusdata.core;

import java.util.Set;


public class SaveChangesRequest extends PersistentStoreRequest {

    private final ChangedObjectsSet m_changedObjects;

    SaveChangesRequest(ChangedObjectsSet changedObjects) {
        m_changedObjects = changedObjects;
    }

    public Set<ManagedObject> getInsertedObjects() {
        return m_changedObjects.getInsertedObjects();
    }

    public Set<ManagedObject> getDeletedObjects() {
        return m_changedObjects.getDeletedObjects();
    }

    public Set<ManagedObject> getUpdatedObjects() {
        return m_changedObjects.getUpdatedObjects();
    }
}
