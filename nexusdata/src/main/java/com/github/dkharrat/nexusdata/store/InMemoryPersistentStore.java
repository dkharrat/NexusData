package com.github.dkharrat.nexusdata.store;

import java.io.File;

import com.github.dkharrat.nexusdata.core.AtomicStore;
import com.github.dkharrat.nexusdata.core.ManagedObject;


public class InMemoryPersistentStore extends AtomicStore {

    long lastUnusedId = 1;

    public InMemoryPersistentStore() {
        super((File)null);
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
