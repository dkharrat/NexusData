package com.pixineers.nexusdata.test;

import java.util.Collection;
import java.util.Set;

import com.pixineers.nexusdata.core.ManagedObject;
import com.pixineers.nexusdata.metamodel.Attributes;
import com.pixineers.nexusdata.metamodel.Attributes.Attribute;
import com.pixineers.nexusdata.metamodel.RelationshipDescription.Type;
import com.pixineers.nexusdata.metamodel.Relationships;
import com.pixineers.nexusdata.metamodel.Relationships.Relationship;

@Attributes({
    @Attribute(name="name",         type=String.class),
})
@Relationships({
    @Relationship(name="employees",   type=Employee.class, relationshipType=Type.TO_MANY, inverseName="company"),
})
public class Company extends ManagedObject {

    String getName() {
        return (String)getValue("name");
    }

    void setName(String name) {
        setValue("name", name);
    }

    Set<Employee> getEmployees() {
        return (Set<Employee>) getValue("employees");
    }

    void addEmployee(Employee employee) {
        getEmployees().add(employee);
    }

    void setEmployees(Collection<Employee> employees) {
        setValue("employees", employees);
    }
}
