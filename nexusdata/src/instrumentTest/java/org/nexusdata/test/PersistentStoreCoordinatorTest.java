package org.nexusdata.test;

import android.test.AndroidTestCase;
import org.nexusdata.core.ObjectContext;
import org.nexusdata.core.PersistentStore;
import org.nexusdata.core.PersistentStoreCoordinator;
import org.nexusdata.metamodel.ObjectModel;
import org.nexusdata.store.AndroidSqlPersistentStore;

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
