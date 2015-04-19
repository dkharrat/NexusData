package com.github.dkharrat.nexusdata.test;

import com.github.dkharrat.nexusdata.core.PersistentStore;
import com.github.dkharrat.nexusdata.store.AndroidSqlPersistentStore;

public class ObjectContextWithSqlStoreTest extends ObjectContextTest {

    @Override
    protected PersistentStore newPersistentStore() {
        return new AndroidSqlPersistentStore(getContext(), getContext().getDatabasePath("test.db"));
    }

}
