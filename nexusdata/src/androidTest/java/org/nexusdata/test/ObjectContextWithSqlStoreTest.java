package org.nexusdata.test;

import org.nexusdata.core.PersistentStore;
import org.nexusdata.store.AndroidSqlPersistentStore;

public class ObjectContextWithSqlStoreTest extends ObjectContextTest {

    @Override
    protected PersistentStore newPersistentStore() {
        return new AndroidSqlPersistentStore(getContext(), getContext().getDatabasePath("test.db"));
    }

}
