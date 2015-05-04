package com.github.dkharrat.nexusdata.test;

import java.util.ArrayList;

import junit.framework.TestCase;
import com.github.dkharrat.nexusdata.metamodel.ObjectModel;
import com.github.dkharrat.nexusdata.metamodel.Entity;

public class ObjectModelTest extends TestCase {

    ObjectModel model;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        model = new ObjectModel(getClass().getResourceAsStream("/assets/company.model.json"), "/assets");
    }

    @Override
    protected void tearDown() throws Exception {
        model = null;

        super.tearDown();
    }

    public void testGetEntities() throws Throwable {
        assertEquals(7, model.getEntities().size());

        ArrayList<String> entityNames = new ArrayList<String>();
        for (Entity<?> entity : model.getEntities()) {
            entityNames.add(entity.getName());
        }
        assertTrue(entityNames.contains(Company.class.getSimpleName()));
        assertTrue(entityNames.contains(Person.class.getSimpleName()));
        assertTrue(entityNames.contains(Employee.class.getSimpleName()));
        assertTrue(entityNames.contains(Address.class.getSimpleName()));
        assertTrue(entityNames.contains(Passport.class.getSimpleName()));
        assertTrue(entityNames.contains(Contractor.class.getSimpleName()));
        assertTrue(entityNames.contains(Director.class.getSimpleName()));
    }

    public void testGetEntity() throws Throwable {
        assertEquals(Company.class.getSimpleName(),  model.getEntity(Company.class).getName());
    }
}
