package com.pixineers.nexusdata.test;

import com.pixineers.nexusdata.core.PersistentStore;
import com.pixineers.nexusdata.store.InMemoryPersistentStore;

public class ObjectContextWithInMemoryStoreTest extends ObjectContextTest {

    @Override
    protected PersistentStore newPersistentStore() {
        return new InMemoryPersistentStore();
    }

}
