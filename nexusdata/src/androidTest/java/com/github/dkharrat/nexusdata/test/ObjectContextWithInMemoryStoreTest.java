package com.github.dkharrat.nexusdata.test;

import com.github.dkharrat.nexusdata.core.PersistentStore;
import com.github.dkharrat.nexusdata.store.InMemoryPersistentStore;

import java.util.Date;
import java.util.List;

public class ObjectContextWithInMemoryStoreTest extends ObjectContextTest {

    @Override
    protected PersistentStore newPersistentStore() {
        return new InMemoryPersistentStore();
    }
}
