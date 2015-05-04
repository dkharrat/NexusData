package com.github.dkharrat.nexusdata.test;

import com.github.dkharrat.nexusdata.metamodel.Attribute;
import com.github.dkharrat.nexusdata.metamodel.Entity;
import com.github.dkharrat.nexusdata.metamodel.ObjectModel;
import com.github.dkharrat.nexusdata.metamodel.Relationship;
import junit.framework.TestCase;

import java.util.*;

public class EntityTest extends TestCase {

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

    private void checkAttributes(Collection<Attribute> actualAttributes, List<AttrInfo> expectedAttributes) {
        Map<String,Pair<Attribute,AttrInfo>> attributeInfoMap = new HashMap<>();

        for (Attribute attr : actualAttributes) {
            Pair<Attribute,AttrInfo> attrInfoPair = new Pair<>(attr, null);
            attributeInfoMap.put(attr.getName(), attrInfoPair);
        }

        for (AttrInfo attrInfo : expectedAttributes) {
            Pair<Attribute,AttrInfo> attrInfoPair = attributeInfoMap.get(attrInfo.name);

            if (attrInfoPair == null) {
                fail("Expected to have attribute " + attrInfo.name);
            }
            attrInfoPair = new Pair<>(attrInfoPair.x, attrInfo);
            attributeInfoMap.put(attrInfo.name, attrInfoPair);

            if (!attrInfo.type.equals(attrInfoPair.x.getType())) {
                fail("Expected attribute " + attrInfo.name + " to have type " + attrInfo.type + " but got " + attrInfoPair.x.getType());
            }
        }

        for (Pair<Attribute,AttrInfo> attrInfoPair : attributeInfoMap.values()) {
            if (attrInfoPair.y == null) {
                fail("Unexpected attribute " + attrInfoPair.x.getName());
            }
        }
    }

    private void checkRelationships(Collection<Relationship> actualRelationships, List<RelationInfo> expectedRelationships) {
        Map<String,Pair<Relationship,RelationInfo>> relationInfoMap = new HashMap<>();

        for (Relationship relation : actualRelationships) {
            Pair<Relationship,RelationInfo> relationInfoPair = new Pair<>(relation, null);
            relationInfoMap.put(relation.getName(), relationInfoPair);
        }

        for (RelationInfo relationInfo : expectedRelationships) {
            Pair<Relationship,RelationInfo> relationInfoPair = relationInfoMap.get(relationInfo.name);

            if (relationInfoPair == null) {
                fail("Expected to have relationship " + relationInfo.name);
            }

            Relationship relationship = relationInfoPair.x;
            relationInfoPair = new Pair<>(relationship, relationInfo);
            relationInfoMap.put(relationInfo.name, relationInfoPair);

            if (!relationInfo.relationType.equals(relationship.getRelationshipType())) {
                fail("Expected relationship " + relationInfo.name + " to be " + relationInfo.relationType + " but got " + relationship.getRelationshipType());
            }

            if (!relationInfo.destEntityName.equals(relationship.getDestinationEntity().getName())) {
                fail("Expected relationship " + relationInfo.name + " to have destination entity named " + relationInfo.destEntityName + " but got " + relationship.getDestinationEntity().getName());
            }

            if (relationInfo.inverseName != null && relationship.getInverse() == null) {
                fail("Expected relationship " + relationInfo.name + " to have inverse named " + relationInfo.inverseName + " but got no inverse");
            }

            if (relationInfo.inverseName == null && relationship.getInverse() != null) {
                fail("Expected relationship " + relationInfo.name + " to not have inverse but got inverse named " + relationship.getInverse().getName());
            }

            if (relationInfo.inverseName != null && !relationInfo.inverseName.equals(relationship.getInverse().getName())) {
                fail("Expected relationship " + relationInfo.name + " to have inverse named " + relationInfo.inverseName + " but got " + relationship.getInverse().getName());
            }
        }

        for (Pair<Relationship,RelationInfo> relationInfoPair : relationInfoMap.values()) {
            if (relationInfoPair.y == null) {
                fail("Unexpected relationship " + relationInfoPair.x.getName());
            }
        }
    }

    public void testGetAttributesForBaseEntity() throws Throwable {
        Entity<Person> personEntity = model.getEntity(Person.class);

        checkAttributes(personEntity.getAttributes(), Arrays.asList(
                new AttrInfo("id",          Integer.class),
                new AttrInfo("firstName",   String.class),
                new AttrInfo("lastName",    String.class),
                new AttrInfo("heightInCm",  Float.class),
                new AttrInfo("dateOfBirth", Date.class)
        ));
    }

    public void testGetAttributesForSubEntity() throws Throwable {
        Entity<Employee> employeeEntity = model.getEntity(Employee.class);

        checkAttributes(employeeEntity.getAttributes(), Arrays.asList(
                new AttrInfo("id",          Integer.class),
                new AttrInfo("firstName",   String.class),
                new AttrInfo("lastName",    String.class),
                new AttrInfo("heightInCm",  Float.class),
                new AttrInfo("hourlyWage",  Double.class),
                new AttrInfo("dateOfBirth", Date.class),
                new AttrInfo("active",      Boolean.class)
        ));
    }

    public void testGetAttributesForSubSubEntity() throws Throwable {
        Entity<Contractor> contractorEntity = model.getEntity(Contractor.class);

        checkAttributes(contractorEntity.getAttributes(), Arrays.asList(
                new AttrInfo("id",          Integer.class),
                new AttrInfo("firstName",   String.class),
                new AttrInfo("lastName",    String.class),
                new AttrInfo("heightInCm",  Float.class),
                new AttrInfo("hourlyWage",  Double.class),
                new AttrInfo("dateOfBirth", Date.class),
                new AttrInfo("active",      Boolean.class),
                new AttrInfo("firmName",    String.class)
        ));
    }

    public void testGetRelationshipsForSubEntity() throws Throwable {
        Entity<Employee> employeeEntity = model.getEntity(Employee.class);

        checkRelationships(employeeEntity.getRelationships(), Arrays.asList(
                new RelationInfo("address",         "Address",  null,               Relationship.Type.TO_ONE),
                new RelationInfo("company",         "Company",  "employees",        Relationship.Type.TO_ONE),
                new RelationInfo("directReports",   "Employee", "manager",          Relationship.Type.TO_MANY),
                new RelationInfo("manager",         "Employee", "directReports",    Relationship.Type.TO_ONE),
                new RelationInfo("passport",        "Passport", "employee",         Relationship.Type.TO_ONE)
        ));
    }

    public void testGetRelationshipsForSubSubEntity() throws Throwable {
        Entity<Contractor> contractorEntity = model.getEntity(Contractor.class);

        checkRelationships(contractorEntity.getRelationships(), Arrays.asList(
                new RelationInfo("address",         "Address",  null,               Relationship.Type.TO_ONE),
                new RelationInfo("company",         "Company",  "employees",        Relationship.Type.TO_ONE),
                new RelationInfo("directReports",   "Employee", "manager",          Relationship.Type.TO_MANY),
                new RelationInfo("manager",         "Employee", "directReports",    Relationship.Type.TO_ONE),
                new RelationInfo("passport",        "Passport", "employee",         Relationship.Type.TO_ONE)
        ));
    }

    public class AttrInfo {
        public final String name;
        public final Class<?> type;

        public AttrInfo(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
    }

    public class RelationInfo {
        public final String name;
        public final String destEntityName;
        public final String inverseName;
        public final Relationship.Type relationType;

        public RelationInfo(String name, String destEntityName, String inverseName, Relationship.Type relationType) {
            this.name = name;
            this.destEntityName = destEntityName;
            this.inverseName = inverseName;
            this.relationType = relationType;
        }
    }

    public class Pair<X,Y> {
        public final X x;
        public final Y y;
        public Pair(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (x != null ? !x.equals(pair.x) : pair.x != null) return false;
            return !(y != null ? !y.equals(pair.y) : pair.y != null);
        }

        @Override
        public int hashCode() {
            int result = x != null ? x.hashCode() : 0;
            result = 31 * result + (y != null ? y.hashCode() : 0);
            return result;
        }
    }
}
