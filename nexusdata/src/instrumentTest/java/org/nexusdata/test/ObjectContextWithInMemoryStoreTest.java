package org.nexusdata.test;

import org.nexusdata.core.PersistentStore;
import org.nexusdata.store.InMemoryPersistentStore;

public class ObjectContextWithInMemoryStoreTest extends ObjectContextTest {

    @Override
    protected PersistentStore newPersistentStore() {
        return new InMemoryPersistentStore();
    }

}
