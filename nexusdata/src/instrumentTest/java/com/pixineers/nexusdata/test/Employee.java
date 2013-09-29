package com.pixineers.nexusdata.test;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;

import com.pixineers.nexusdata.core.ManagedObject;
import com.pixineers.nexusdata.metamodel.Attributes;
import com.pixineers.nexusdata.metamodel.Attributes.Attribute;
import com.pixineers.nexusdata.metamodel.RelationshipDescription.Type;
import com.pixineers.nexusdata.metamodel.Relationships;
import com.pixineers.nexusdata.metamodel.Relationships.Relationship;

@Attributes({
    @Attribute(name="firstName",        type=String.class),
    @Attribute(name="lastName",         type=String.class),
    @Attribute(name="salary",           type=Integer.class),
    @Attribute(name="dateOfBirth",      type=Date.class),
})
@Relationships({
    @Relationship(name="company",           type=Company.class,     relationshipType=Type.TO_ONE,   inverseName="employees"),
    @Relationship(name="manager",           type=Employee.class,    relationshipType=Type.TO_ONE,   inverseName="directReports"),
    @Relationship(name="directReports",     type=Employee.class,    relationshipType=Type.TO_MANY,  inverseName="manager"),
    @Relationship(name="address",           type=Address.class,     relationshipType=Type.TO_ONE,   inverseName="employee"),
})
public class Employee extends ManagedObject {
    public String getFirstName() {
        return (String)getValue("firstName");
    }

    public void setFirstName(String firstName) {
        setValue("firstName", firstName);
    }

    public String getLastName() {
        return (String)getValue("lastName");
    }

    public void setLastName(String lastName) {
        setValue("lastName", lastName);
    }

    public Company getCompany() {
        return (Company)getValue("company");
    }

    public void setCompany(Company company) {
        setValue("company", company);
    }

    public Employee getManager() {
        return (Employee)getValue("manager");
    }

    public void setManager(Employee employee) {
        setValue("manager", employee);
    }

    public Set<Employee> getDirectReports() {
        return (Set<Employee>)getValue("directReports");
    }

    public Address getAddress() {
        return (Address)getValue("address");
    }

    public void setAddress(Address address) {
        setValue("address", address);
    }

    public int getSalary() {
        return (Integer)getValue("salary");
    }

    public void setSalary(int salary) {
        setValue("salary", salary);
    }

    public Date getDateOfBirth() {
        return (Date)getValue("dateOfBirth");
    }

    public void setDateOfBirth(Date dateOfBirth) {
        setValue("dateOfBirth", dateOfBirth);
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public static Comparator<? super Employee> getComparator() {
        return new Comparator<Employee>() {
            @Override
            public int compare(Employee lhs, Employee rhs) {
                return lhs.getFullName().compareTo(rhs.getFullName());
            }
        };
    }
}
