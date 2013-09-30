package com.pixineers.nexusdata.test;

import android.test.AndroidTestCase;

import com.pixineers.nexusdata.core.ObjectContext;
import com.pixineers.nexusdata.core.PersistentStore;
import com.pixineers.nexusdata.core.PersistentStoreCoordinator;
import com.pixineers.nexusdata.metamodel.ObjectModel;
import com.pixineers.nexusdata.store.AndroidSqlPersistentStore;

public class PersistentStoreCoordinatorTest extends AndroidTestCase {

    PersistentStoreCoordinator coordinator;
    ObjectContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Class<?>[] types = {Employee.class, Company.class, Address.class};
        ObjectModel model = new ObjectModel(types, 1);
        coordinator = new PersistentStoreCoordinator(model);
        PersistentStore persistentStore = new AndroidSqlPersistentStore(getContext(), getContext().getDatabasePath("test.db"));
        coordinator.addStore(persistentStore);
        context = new ObjectContext(coordinator);
    }

    @Override
    protected void tearDown() throws Exception {
        coordinator.getPersistentStores().get(0).getLocation().delete();
        coordinator = null;
        context = null;

        super.tearDown();
    }

    public void testGetUuidToObjectIDConversion() throws Throwable {
        Employee employee = context.newObject(Employee.class);
        context.save();

        assertEquals(employee.getID(), coordinator.objectIDFromUri(employee.getID().getUriRepresentation()));
    }

    public void testCantConvertTemporaryUuidToObjectID() throws Throwable {
        Employee employee = context.newObject(Employee.class);

        boolean thrown = false;
        try {
            coordinator.objectIDFromUri(employee.getID().getUriRepresentation());
        } catch (IllegalArgumentException e) {
            thrown = true;
        }

        assertTrue(thrown);
    }
}
