package com.github.dkharrat.nexusdata.test;

import com.github.dkharrat.nexusdata.core.PersistentStore;
import com.github.dkharrat.nexusdata.store.InMemoryPersistentStore;

public class ObjectContextWithInMemoryStoreTest extends ObjectContextTest {

    @Override
    protected PersistentStore newPersistentStore() {
        return new InMemoryPersistentStore();
    }

}
