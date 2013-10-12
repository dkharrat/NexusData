package org.nexusdata.store;

import java.util.UUID;

import org.nexusdata.core.AtomicStore;
import org.nexusdata.core.ManagedObject;


public class InMemoryPersistentStore extends AtomicStore {

    long lastUnusedId = 1;

    public InMemoryPersistentStore() {
        super(null);
    }

    @Override
    protected void loadMetadata() {
        setUuid(UUID.randomUUID());
    }

    @Override
    public void load() {
        // Nothing to load
    }

    @Override
    public void save() {
        // Nothing to save
    }

    @Override
    public Object createReferenceObjectForManagedObject(ManagedObject object) {
        return lastUnusedId++;
    }
}
