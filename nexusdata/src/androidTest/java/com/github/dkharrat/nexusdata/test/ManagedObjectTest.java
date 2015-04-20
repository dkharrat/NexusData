package com.github.dkharrat.nexusdata.test;

import java.util.Arrays;
import java.util.Set;
import junit.framework.TestCase;
import com.github.dkharrat.nexusdata.core.ObjectContext;
import com.github.dkharrat.nexusdata.core.PersistentStore;
import com.github.dkharrat.nexusdata.core.PersistentStoreCoordinator;
import com.github.dkharrat.nexusdata.metamodel.ObjectModel;
import com.github.dkharrat.nexusdata.store.InMemoryPersistentStore;

public class ManagedObjectTest extends TestCase {

    ObjectContext context;
    Passport japanesePassport;
    Employee john;
    Company google;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ObjectModel model = new ObjectModel(getClass().getResourceAsStream("/assets/company.model.json"));
        PersistentStoreCoordinator coordinator = new PersistentStoreCoordinator(model);
        PersistentStore persistentStore = new InMemoryPersistentStore();
        coordinator.addStore(persistentStore);
        context = new ObjectContext(coordinator);

        japanesePassport = context.newObject(Passport.class);
        japanesePassport.setNumber("123");
        japanesePassport.setCountry("Japan");

        john = context.newObject(Employee.class);
        john.setId(123);
        john.setFirstName("John");
        john.setPassport(japanesePassport);
        Employee mike = context.newObject(Employee.class);
        mike.setFirstName("mike");

        google = context.newObject(Company.class);
        google.setName("Google");
        google.setEmployees(Arrays.asList(john, mike));
    }

    @Override
    protected void tearDown() throws Exception {
        context = null;
        super.tearDown();
    }

    public void testDefaultsSetWhenCreated() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        assertTrue(bob.isActive());
        assertEquals(10.123, bob.getHourlyWage());
    }

    public void testIsInserted() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        assertTrue(bob.isInserted());
    }

    public void testIsInsertedIsFalseAfterSave() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        context.save();
        assertFalse(bob.isInserted());
    }

    public void testIsUpdated() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        context.save();

        bob.setFirstName("Rob");
        assertTrue(bob.isUpdated());
    }

    public void testIsDeleted() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        context.save();

        assertFalse(bob.isDeleted());
        context.delete(bob);
        assertTrue(bob.isDeleted());
    }

    public void testSettingAttribute() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        bob.setFirstName("Bob");
        bob.setId(999);
        bob.setHourlyWage(38.0000000000000004);
        bob.setHeightInCm(170.26f);

        assertEquals("Bob", bob.getFirstName());
        assertEquals(999, bob.getId());
        assertEquals(38.0000000000000004, bob.getHourlyWage());
        assertEquals(170.26f, bob.getHeightInCm());
    }

    public void testSettingOneToOneRelationship() throws Throwable {
        Passport canadianPassport = context.newObject(Passport.class);
        canadianPassport.setNumber("123");
        canadianPassport.setCountry("Canada");

        Employee bob = context.newObject(Employee.class);
        bob.setFirstName("Bob");
        bob.setPassport(canadianPassport);

        assertSame(canadianPassport, bob.getPassport());
        assertSame(bob, canadianPassport.getEmployee());
    }

    public void testSettingOneToOneRelationshipWithNoInverse() throws Throwable {
        Address canadianAddress = context.newObject(Address.class);
        canadianAddress.setCountry("Canada");

        Employee bob = context.newObject(Employee.class);
        bob.setFirstName("Bob");
        bob.setAddress(canadianAddress);

        assertSame(canadianAddress, bob.getAddress());
    }

    public void testSettingOneToManyReflexiveRelationship() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        bob.setFirstName("Bob");
        bob.setManager(john);

        Employee alice = context.newObject(Employee.class);
        alice.setFirstName("Alice");
        alice.setManager(john);

        assertSame(john, bob.getManager());
        assertTrue(bob.getDirectReports().isEmpty());

        assertSame(john, alice.getManager());
        assertTrue(alice.getDirectReports().isEmpty());

        assertSame(null, john.getManager());
        assertTrue(john.getDirectReports().containsAll(Arrays.asList(bob, alice)));
    }

    public void testOverridingOneToOneRelationship() throws Throwable {
        Passport canadianPassport = context.newObject(Passport.class);
        canadianPassport.setNumber("123");
        canadianPassport.setCountry("Canada");

        john.setPassport(canadianPassport);

        assertSame(canadianPassport, john.getPassport());
        assertSame(john, canadianPassport.getEmployee());
        assertSame(null, japanesePassport.getEmployee());
    }

    public void testClearingOneToOneRelationship() throws Throwable {
        john.setPassport(null);

        assertSame(null, john.getPassport());
        assertSame(null, japanesePassport.getEmployee());
    }

    public void testSettingToManyRelationship() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        bob.setFirstName("Bob");
        google.setEmployees(Arrays.asList(bob));

        assertTrue(google.getEmployees().contains(bob));
        assertEquals(1, google.getEmployees().size());
        assertSame(google, bob.getCompany());
        assertSame(null, john.getCompany());
    }

    public void testAddingToManyRelationship() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        bob.setFirstName("Bob");
        google.addEmployee(bob);

        assertTrue(google.getEmployees().contains(bob));
        assertEquals(3, google.getEmployees().size());
        assertSame(google, bob.getCompany());
    }

    public void testClearingToManyRelationship() throws Throwable {
        google.setEmployees((Set<Employee>)null);

        assertFalse(google.getEmployees().contains(john));
        assertTrue(google.getEmployees().isEmpty());
        assertSame(null, john.getCompany());
    }

    public void testRemovingFromToManyRelationship() throws Throwable {
        google.getEmployees().remove(john);

        assertFalse(google.getEmployees().contains(john));
        assertEquals(1, google.getEmployees().size());
        assertSame(null, john.getCompany());
    }

    public void testSettingToOneRelationship() throws Throwable {
        Employee bob = context.newObject(Employee.class);
        bob.setFirstName("Bob");
        bob.setCompany(google);

        assertSame(google, bob.getCompany());
        assertTrue(google.getEmployees().contains(bob));
        assertEquals(3, google.getEmployees().size());
    }

    public void testClearingToOneRelationship() throws Throwable {
        john.setCompany(null);

        assertSame(null, john.getCompany());
        assertFalse(google.getEmployees().contains(john));
        assertEquals(1, google.getEmployees().size());
    }

    public void testChangingToOneRelationship() throws Throwable {
        Company apple = context.newObject(Company.class);
        apple.setName("Apple");
        john.setCompany(apple);

        assertSame(apple, john.getCompany());
        assertFalse(google.getEmployees().contains(john));
        assertEquals(1, google.getEmployees().size());
        assertTrue(apple.getEmployees().contains(john));
        assertEquals(1, apple.getEmployees().size());
    }
}
