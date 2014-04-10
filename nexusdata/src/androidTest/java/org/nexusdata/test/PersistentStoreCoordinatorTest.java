package com.github.dkharrat.nexusdata.test;

import java.io.File;
import android.test.AndroidTestCase;
import com.github.dkharrat.nexusdata.core.ObjectContext;
import com.github.dkharrat.nexusdata.core.PersistentStore;
import com.github.dkharrat.nexusdata.core.PersistentStoreCoordinator;
import com.github.dkharrat.nexusdata.metamodel.ObjectModel;
import com.github.dkharrat.nexusdata.store.AndroidSqlPersistentStore;

public class PersistentStoreCoordinatorTest extends AndroidTestCase {

    PersistentStoreCoordinator coordinator;
    ObjectContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ObjectModel model = new ObjectModel(getClass().getResourceAsStream("/assets/company.model.json"));
        coordinator = new PersistentStoreCoordinator(model);
        PersistentStore persistentStore = new AndroidSqlPersistentStore(getContext(), getContext().getDatabasePath("test.db"));
        coordinator.addStore(persistentStore);
        context = new ObjectContext(coordinator);
    }

    @Override
    protected void tearDown() throws Exception {
        new File(coordinator.getPersistentStores().get(0).getLocation().toURI()).delete();
        coordinator = null;
        context = null;

        super.tearDown();
    }

    public void testGetUuidToObjectIDConversion() throws Throwable {
        Employee employee = context.newObject(Employee.class);
        employee.setFirstName("John");
        employee.setLastName("Smith");
        context.save();

        assertEquals(employee.getID(), coordinator.objectIDFromUri(employee.getID().getUriRepresentation()));
    }

    public void testCantConvertTemporaryUuidToObjectID() throws Throwable {
        Employee employee = context.newObject(Employee.class);
        employee.setFirstName("John");
        employee.setLastName("Smith");

        boolean thrown = false;
        try {
            coordinator.objectIDFromUri(employee.getID().getUriRepresentation());
        } catch (IllegalArgumentException e) {
            thrown = true;
        }

        assertTrue(thrown);
    }
}
