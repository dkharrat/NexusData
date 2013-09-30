package com.pixineers.nexusdata.test;

import com.pixineers.nexusdata.core.ManagedObject;
import com.pixineers.nexusdata.metamodel.Attributes;
import com.pixineers.nexusdata.metamodel.Attributes.Attribute;
import com.pixineers.nexusdata.metamodel.RelationshipDescription.Type;
import com.pixineers.nexusdata.metamodel.Relationships;
import com.pixineers.nexusdata.metamodel.Relationships.Relationship;

@Attributes({
    @Attribute(name=Address.Property.STEET_NAME,   type=String.class),
    @Attribute(name=Address.Property.COUNTRY,      type=String.class),
})
@Relationships({
    @Relationship(name=Address.Property.EMPLOYEE,   type=Employee.class, relationshipType=Type.TO_ONE, inverseName="address"),
})
public class Address extends ManagedObject {

    public interface Property {
        final static String STEET_NAME      = "streetName";
        final static String COUNTRY         = "country";
        final static String EMPLOYEE        = "employee";
    }

    public String getStreetName() {
        return (String)getValue(Property.STEET_NAME);
    }

    public void setStreetName(String streetName) {
        setValue(Property.STEET_NAME, streetName);
    }

    public String getCountry() {
        return (String)getValue(Property.COUNTRY);
    }

    public void setCountry(String country) {
        setValue(Property.COUNTRY, country);
    }

    public Employee getEmployee() {
        return (Employee)getValue(Property.EMPLOYEE);
    }

    public void setEmployee(Employee employee) {
        setValue(Property.EMPLOYEE, employee);
    }
}
