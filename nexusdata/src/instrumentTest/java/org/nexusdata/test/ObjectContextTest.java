package org.nexusdata.test;

import java.lang.RuntimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.test.AndroidTestCase;

import org.nexusdata.core.ChangedObjectsSet;
import org.nexusdata.core.FetchRequest;
import org.nexusdata.core.ObjectContext;
import org.nexusdata.core.ObjectContextNotifier;
import org.nexusdata.core.ObjectContextNotifier.DefaultObjectContextListener;
import org.nexusdata.core.ObjectContextNotifier.ObjectContextListener;
import org.nexusdata.core.PersistentStore;
import org.nexusdata.core.PersistentStoreCoordinator;
import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.metamodel.ObjectModel;
import org.nexusdata.predicate.ExpressionBuilder;

public abstract class ObjectContextTest extends AndroidTestCase {

    ObjectModel model;
    PersistentStore persistentStore;
    ObjectContext mainContext;

    protected abstract PersistentStore newPersistentStore();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        model = new ObjectModel(getClass().getResourceAsStream("/assets/company.model.json"));
        PersistentStoreCoordinator coordinator = new PersistentStoreCoordinator(model);
        persistentStore = newPersistentStore();
        coordinator.addStore(persistentStore);
        mainContext = new ObjectContext(coordinator);
    }

    @Override
    protected void tearDown() throws Exception {
        if (persistentStore.getLocation() != null) persistentStore.getLocation().delete();
        persistentStore = null;
        mainContext = null;

        super.tearDown();
    }

    public void testInsertNewObject() throws Throwable {

        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000, true, new Date(999000));
        assertTrue(mainContext.getInsertedObjects().contains(employee1));
        mainContext.save();

        List<Employee> employees = mainContext.findAll(Employee.class);

        assertSame(mainContext, employees.get(0).getObjectContext());
        assertEquals(1, employees.size());

        // fetching again should fetch the same object references
        List<Employee> employees2 = mainContext.findAll(Employee.class);
        assertEquals(1, employees2.size());
        assertSame(employees.get(0), employees2.get(0));
        assertEquals("John", employees.get(0).getFirstName());
        assertEquals("Smith", employees.get(0).getLastName());
        assertEquals(1000, employees.get(0).getSalary());
        assertEquals(true, employees.get(0).isActive());
        assertEquals(new Date(999000), employees.get(0).getDateOfBirth());
    }

    public void testDeleteThenInsertIsNoOp() throws Throwable {
        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        mainContext.save();

        mainContext.delete(employee1);
        mainContext.insert(employee1);

        assertFalse(mainContext.hasChanges());
    }

    public void testContextReturnsSameObjectAfterSave() throws Throwable {

        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        mainContext.save();
        List<Employee> employees = mainContext.findAll(Employee.class);

        assertSame(employee1, employees.get(0));
    }

    public void testFetchExistingObjectFromDifferentContext() throws Throwable {

        Company google = createCompany(mainContext, "Google");
        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        google.addEmployee(employee1);
        mainContext.save();

        ObjectContext context2 = new ObjectContext(persistentStore.getCoordinator());
        List<Employee> employees = context2.findAll(Employee.class);

        assertEquals(1, employees.size());
        assertEmployeesEqual(employee1, employees.get(0));
        assertNotSame(employee1, employees.get(0));

        List<Company> companies = context2.findAll(Company.class);
        assertEquals(1, companies.size());
        assertEquals(Arrays.asList(employees.get(0)), new ArrayList<Employee>(companies.get(0).getEmployees()));
    }

    public void testFetchUsingPredicate() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        Employee employee1 = createEmployee(context, "John", "Smith", 1000);
        Employee employee2 = createEmployee(context, "Mike", "Jones", 1000);
        context.save();

        @SuppressWarnings("unchecked")
        EntityDescription<Employee> employeeEntity = (EntityDescription<Employee>) employee1.getEntity();


        FetchRequest<Employee> fetchRequest = new FetchRequest<Employee>(employeeEntity);
        fetchRequest.setPredicate(ExpressionBuilder.field("firstName").eq("Mike").getPredicate());
        List<Employee> employees = mainContext.executeFetchOperation(fetchRequest);

        assertEquals(1, employees.size());
        assertEmployeesEqual(employee2, employees.get(0));
    }

    public void testFetchUsingPredicateHavingManagedObjectComparison() throws Throwable {
        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        Company google = createCompany(context, "Google");
        Company microsoft = createCompany(context, "Microsoft");

        Employee employee1 = createEmployee(context, "John", "Smith", 1000);
        Employee employee2 = createEmployee(context, "Mike", "Jones", 1000);
        assertEquals(null, employee1.getCompany());

        google.addEmployee(employee1);
        microsoft.addEmployee(employee2);

        context.save();

        @SuppressWarnings("unchecked")
        EntityDescription<Employee> employeeEntity = (EntityDescription<Employee>) employee1.getEntity();

        // we need the 'google' object to be  based on the mainContext, since that's the context we're using to
        // create the predicate
        google = (Company)mainContext.objectWithID(google.getID());

        FetchRequest<Employee> fetchRequest = new FetchRequest<Employee>(employeeEntity);
        fetchRequest.setPredicate(ExpressionBuilder.field("company").eq(google).getPredicate());
        List<Employee> employees = mainContext.executeFetchOperation(fetchRequest);
        assertEquals(1, employees.size());
        assertEmployeesEqual(employee1, employees.get(0));
    }

    public void testFetchWithLimit() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        createEmployee(context, "John", "Smith", 1000);
        createEmployee(context, "Mike", "Jones", 1000);
        context.save();

        FetchRequest<Employee> fetchRequest = new FetchRequest<Employee>(model.getEntity(Employee.class));
        fetchRequest.setLimit(1);
        List<Employee> employees = mainContext.executeFetchOperation(fetchRequest);

        assertEquals(1, employees.size());
    }

    public void testFetchWithLimitThatExceedsResults() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        createEmployee(context, "John", "Smith", 1000);
        createEmployee(context, "Mike", "Jones", 1000);
        context.save();

        FetchRequest<Employee> fetchRequest = new FetchRequest<Employee>(model.getEntity(Employee.class));
        fetchRequest.setLimit(3);
        List<Employee> employees = mainContext.executeFetchOperation(fetchRequest);

        assertEquals(2, employees.size());
    }

    public void testFetchWithSortDescriptor() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        createEmployee(context, "Mike", "Dale", 1000);
        createEmployee(context, "John", "Smith", 1000);
        createEmployee(context, "Matthew", "Roberts", 1000);
        createEmployee(context, "Mike", "Jones", 1000);
        context.save();

        FetchRequest<Employee> fetchRequest = mainContext.newFetchRequestBuilder(Employee.class).sortBy("firstName", true).sortBy("lastName", false).build();
        List<Employee> employees = mainContext.executeFetchOperation(fetchRequest);

        assertEquals("John Smith", employees.get(0).getFullName());
        assertEquals("Matthew Roberts", employees.get(1).getFullName());
        assertEquals("Mike Jones", employees.get(2).getFullName());
        assertEquals("Mike Dale", employees.get(3).getFullName());
    }

    public void testFetchUsingPredicateOfInsertedObjects() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        createEmployee(context, "Mike", "Smith", 1000);
        context.save();

        createEmployee(mainContext, "Mike", "Jones", 1000);
        FetchRequest<Employee> fetchRequest = mainContext.newFetchRequestBuilder(Employee.class).build();
        fetchRequest.setPredicate(ExpressionBuilder.field("firstName").eq("Mike").getPredicate());
        List<Employee> employees = mainContext.executeFetchOperation(fetchRequest);

        assertEquals(2, employees.size());
    }

    public void testUpdateExistingObject() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        Company google = createCompany(context, "Google");
        Employee employee = createEmployee(context, "John", "Smith", 1000);
        employee.setCompany(google);
        context.save();

        List<Employee> employees = mainContext.findAll(Employee.class);
        employee = (Employee)mainContext.objectWithID(employee.getID());
        google = (Company)mainContext.objectWithID(google.getID());

        google.setName("Google, Inc.");
        employee.setFirstName("Johnny");
        employee.setDateOfBirth(new Date(888000));

        mainContext.save();

        employees = mainContext.findAll(Employee.class);
        Employee updatedEmployee = employees.get(0);

        assertSame(mainContext, employee.getObjectContext());
        assertEquals(1, employees.size());
        assertEquals("Johnny Smith", updatedEmployee.getFullName());
        assertEquals(new Date(888000), updatedEmployee.getDateOfBirth());

        assertEquals("Google, Inc.", google.getName());
        assertEquals(Arrays.asList(employee), new ArrayList<Employee>(google.getEmployees()));
    }

    public void testRefreshObject() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        createEmployee(context, "John", "Smith", 1000);
        context.save();

        List<Employee> employees = mainContext.findAll(Employee.class);
        Employee employee = employees.get(0);

        employee.setFirstName("Johnny");
        employee.refresh();
        assertEquals("John", employee.getFirstName());
    }

    public void testUpdateExistingObjectAfterFetch() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        createEmployee(context, "John", "Smith", 1000);
        context.save();

        List<Employee> employees = mainContext.findAll(Employee.class);
        Employee employee = employees.get(0);

        employee.setFirstName("Johnny");
        assertTrue(mainContext.getUpdatedObjects().contains(employee));
        mainContext.save();

        assertEquals(employee.getFirstName() + " " + employee.getLastName(), "Johnny Smith");
    }

    public void testContextHasNoChangesAfterSave() throws Throwable {
        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        createEmployee(context, "John", "Smith", 1000);

        assertTrue(context.hasChanges());
        context.save();
        assertFalse(context.hasChanges());
    }

    public void testObjectNotMarkedAsUpdatedWithUpdateWithSameValue() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        createEmployee(context, "John", "Smith", 1000);
        context.save();

        List<Employee> employees = mainContext.findAll(Employee.class);
        Employee employee1 = employees.get(0);

        employee1.setSalary(1000);
        assertFalse(mainContext.getUpdatedObjects().contains(employee1));
    }

    public void testObjectsAreMarkedUpdatedForSettingToManyRelationship() throws Throwable {
        Company google = createCompany(mainContext, "Google");
        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        Employee employee2 = createEmployee(mainContext, "Mike", "Jones", 1000);
        assertEquals(null, employee1.getCompany());

        google.setEmployees(Arrays.asList(employee1, employee2));
        mainContext.save();

        google.setEmployees(Arrays.asList(employee1));

        assertEquals(2, mainContext.getUpdatedObjects().size());
        assertTrue(mainContext.getUpdatedObjects().containsAll(Arrays.asList(google, employee2)));
    }

    public void testObjectsAreMarkedUpdatedForSettingToOneRelationship() throws Throwable {
        Company google = createCompany(mainContext, "Google");
        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);

        mainContext.save();

        employee1.setCompany(google);

        assertEquals(2, mainContext.getUpdatedObjects().size());
        assertTrue(mainContext.getUpdatedObjects().containsAll(Arrays.asList(google, employee1)));
    }

    public void testObjectsAreMarkedUpdatedForClearingToOneRelationship() throws Throwable {
        Company google = createCompany(mainContext, "Google");
        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        employee1.setCompany(google);

        mainContext.save();
        employee1.setCompany(null);

        assertEquals(2, mainContext.getUpdatedObjects().size());
        assertTrue(mainContext.getUpdatedObjects().containsAll(Arrays.asList(google, employee1)));
    }

    public void testObjectsAreMarkedUpdatedForAddingToRelationship() throws Throwable {
        Company google = createCompany(mainContext, "Google");
        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        Employee employee2 = createEmployee(mainContext, "Mike", "Jones", 1000);
        assertEquals(null, employee1.getCompany());

        google.setEmployees(Arrays.asList(employee1));
        mainContext.save();

        google.addEmployee(employee2);

        assertEquals(2, mainContext.getUpdatedObjects().size());
        assertTrue(mainContext.getUpdatedObjects().containsAll(Arrays.asList(google, employee2)));
    }

    public void testObjectsAreMarkedUpdatedForRemovingFromRelationship() throws Throwable {
        Company google = createCompany(mainContext, "Google");
        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        Employee employee2 = createEmployee(mainContext, "Mike", "Jones", 1000);
        assertEquals(null, employee1.getCompany());

        google.setEmployees(Arrays.asList(employee1, employee2));
        mainContext.save();

        google.getEmployees().remove(employee2);

        assertEquals(2, mainContext.getUpdatedObjects().size());
        assertTrue(mainContext.getUpdatedObjects().containsAll(Arrays.asList(google, employee2)));
    }

    public void testObjectsAreNotMarkedUpdatedForAlreadyEmptyRelationshipChanges() throws Throwable {
        Company google = createCompany(mainContext, "Google");
        mainContext.save();

        google.setEmployees((Set<Employee>)null);

        assertTrue(mainContext.getUpdatedObjects().isEmpty());
    }

    public void testObjectsAreNotMarkedUpdatedForNoRelationshipChanges() throws Throwable {
        Company google = createCompany(mainContext, "Google");

        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        Employee employee2 = createEmployee(mainContext, "Mike", "Jones", 1000);
        assertEquals(null, employee1.getCompany());

        google.setEmployees(Arrays.asList(employee1, employee2));
        mainContext.save();

        google.setEmployees(Arrays.asList(employee1, employee2));

        assertTrue(mainContext.getUpdatedObjects().isEmpty());
    }

    public void testDeleteExistingObject() throws Throwable {

        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        Company google = createCompany(context, "Google");
        Employee employee1 = createEmployee(context, "John", "Smith", 1000);
        employee1.setCompany(google);
        context.save();

        mainContext.delete(employee1);
        mainContext.save();

        assertSame(null, employee1.getObjectContext());

        List<Employee> employees = mainContext.findAll(Employee.class);

        assertTrue(employees.isEmpty());
        assertTrue(google.getEmployees().isEmpty());
    }

    public void testInsertWithToManyRelationship() throws Throwable {
        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        Company google = createCompany(context, "Google");

        Employee employee1 = createEmployee(context, "John", "Smith", 1000);
        Employee employee2 = createEmployee(context, "Mike", "Jones", 1000);
        assertEquals(null, employee1.getCompany());

        google.addEmployee(employee1);
        google.addEmployee(employee2);

        context.save();

        List<Employee> employees = mainContext.findAll(Employee.class);
        Collections.sort(employees, Employee.getComparator());

        assertEquals(2, employees.size());
        assertEquals(employee1.getFullName(), employees.get(0).getFullName());
        assertEquals(employee2.getFullName(), employees.get(1).getFullName());

        assertEquals(employee1.getCompany().getName(), employees.get(0).getCompany().getName());
        assertEquals(employee1.getCompany().getName(), employees.get(1).getCompany().getName());
        assertTrue(employees.get(0).getCompany().getEmployees().containsAll(employees));

        List<Company> companies = mainContext.findAll(Company.class);

        assertEquals(2, companies.get(0).getEmployees().size());
        assertTrue(companies.get(0).getEmployees().containsAll(employees));
    }

    public void testInsertWithToOneRelationship() throws Throwable {
        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        Employee employee1 = createEmployee(context, "John", "Smith", 1000);

        Address address = context.newObject(Address.class);
        address.setCountry("Japan");
        employee1.setAddress(address);

        context.save();

        List<Employee> employees = mainContext.findAll(Employee.class);

        assertEquals(employee1.getAddress().getCountry(), employees.get(0).getAddress().getCountry());
        assertEquals(employee1.getFullName(), employees.get(0).getAddress().getEmployee().getFullName());
    }

    public void testInsertWithOneToManyReflexiveRelationship() throws Throwable {
        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        Employee john = createEmployee(context, "John", "Smith", 1000);
        Employee bob = createEmployee(context, "Bob",  "Black", 1000);
        Employee alice = createEmployee(context, "Alice",  "White", 1000);

        bob.setManager(john);
        alice.setManager(john);

        context.save();

        john = mainContext.findAll(Employee.class, ExpressionBuilder.field("firstName").eq("John").getPredicate()).get(0);
        List<Employee> johnsDirectReports = new ArrayList<Employee>(john.getDirectReports());
        Collections.sort(johnsDirectReports, Employee.getComparator());

        alice = johnsDirectReports.get(0);
        bob = johnsDirectReports.get(1);

        assertSame(null, john.getManager());
        assertSame(2, johnsDirectReports.size());
        assertEquals("Alice", johnsDirectReports.get(0).getFirstName());
        assertEquals("Bob",   johnsDirectReports.get(1).getFirstName());

        assertSame(john, bob.getManager());
        assertTrue(bob.getDirectReports().isEmpty());

        assertSame(john, alice.getManager());
        assertTrue(alice.getDirectReports().isEmpty());
    }

    public void testContextReturnsSameObjectFromObjectIDUri() throws Throwable {

        Employee employee1 = createEmployee(mainContext, "John", "Smith", 1000);
        mainContext.save();

        assertSame(employee1, mainContext.objectWithID(employee1.getID().getUriRepresentation()));
    }

    public void testContextMergesChangesProperly() throws Throwable {
        // first, let's create a company through a temporary context
        ObjectContext context = new ObjectContext(persistentStore.getCoordinator());
        Company google = createCompany(context, "Google");
        context.save();

        // get company and its employees from the main context to make sure it's in memory
        Company googleFromMainContext = (Company) mainContext.getExistingObject(google.getID());
        googleFromMainContext.getEmployees().toArray();

        // now makes some changes and add an employee to the company and an associated address
        google.setName("Google, Inc.");
        Employee employee1 = createEmployee(context, "John", "Smith", 1000);
        google.addEmployee(employee1);

        Address address = context.newObject(Address.class);
        address.setCountry("Japan");
        employee1.setAddress(address);

        // register a listener before we save, which will merge changes after a save
        ObjectContextListener listener = new DefaultObjectContextListener() {
            @Override
            public void contextDidSave(ObjectContext context, ChangedObjectsSet changedObjects) {
                mainContext.mergeChangesFromSaveNotification(changedObjects);
            }
        };

        ObjectContextNotifier.registerListener(context, listener);
        context.save();

        // at this point, the changes from the temporary context should be merged to the main context
        assertEquals(1, googleFromMainContext.getEmployees().size());
        assertEquals("Google, Inc.", googleFromMainContext.getName());

        Employee employeeFromMainContext = (Employee) mainContext.getExistingObject(employee1.getID());
        assertTrue(googleFromMainContext.getEmployees().contains(employeeFromMainContext));
        assertEquals(employee1.getAddress().getID(), employeeFromMainContext.getAddress().getID());

        ObjectContextNotifier.unregisterListener(listener);
    }

    static private void assertEmployeesEqual(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getSalary(), actual.getSalary());
        assertEquals(expected.isActive(), actual.isActive());
        if (expected.getCompany() == null) {
            assertSame(null, actual.getCompany());
        } else {
            assertEquals(expected.getCompany().getName(), actual.getCompany().getName());
        }
    }

    private Employee createEmployee(ObjectContext context, String firstName, String lastName, int salary, boolean active, Date dateOfBirth) {
        Employee employee = context.newObject(Employee.class);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setSalary(salary);
        employee.setActive(active);
        if (dateOfBirth != null) employee.setDateOfBirth(dateOfBirth);

        return employee;
    }

    private Employee createEmployee(ObjectContext context, String firstName, String lastName, int salary) {
        return createEmployee(context, firstName, lastName, salary, true, null);
    }

    private Company createCompany(ObjectContext context, String name) {
        Company company = context.newObject(Company.class);
        company.setName(name);

        return company;
    }
}
