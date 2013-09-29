package com.pixineers.nexusdata.test;

import com.pixineers.nexusdata.core.PersistentStore;
import com.pixineers.nexusdata.store.AndroidSqlPersistentStore;

public class ObjectContextWithSqlStoreTest extends ObjectContextTest {

    @Override
    protected PersistentStore newPersistentStore() {
        return new AndroidSqlPersistentStore(getContext(), getContext().getDatabasePath("test.db"));
    }

}
