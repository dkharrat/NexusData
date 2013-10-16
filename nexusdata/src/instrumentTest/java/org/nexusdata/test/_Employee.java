// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package org.nexusdata.test;

import java.util.Date;
import java.util.Set;
import org.nexusdata.core.ManagedObject;

class _Employee extends ManagedObject {

    public interface Property {
        final static String FIRST_NAME = "firstName";
        final static String LAST_NAME = "lastName";
        final static String SALARY = "salary";
        final static String ACTIVE = "active";
        final static String DATE_OF_BIRTH = "dateOfBirth";
        final static String COMPANY = "company";
        final static String MANAGER = "manager";
        final static String DIRECT_REPORTS = "directReports";
        final static String ADDRESS = "address";
    }


    public String getFirstName() {
        return (String)getValue(Property.FIRST_NAME);
    }

    public void setFirstName(String firstName) {
        setValue(Property.FIRST_NAME, firstName);
    }

    public String getLastName() {
        return (String)getValue(Property.LAST_NAME);
    }

    public void setLastName(String lastName) {
        setValue(Property.LAST_NAME, lastName);
    }

    public int getSalary() {
        return (Integer)getValue(Property.SALARY);
    }

    public void setSalary(int salary) {
        setValue(Property.SALARY, salary);
    }

    public boolean isActive() {
        return (Boolean)getValue(Property.ACTIVE);
    }

    public void setActive(boolean active) {
        setValue(Property.ACTIVE, active);
    }

    public Date getDateOfBirth() {
        return (Date)getValue(Property.DATE_OF_BIRTH);
    }

    public void setDateOfBirth(Date dateOfBirth) {
        setValue(Property.DATE_OF_BIRTH, dateOfBirth);
    }


    public Company getCompany() {
        return (Company)getValue(Property.COMPANY);
    }

    public void setCompany(Company company) {
        setValue(Property.COMPANY, company);
    }

    public Employee getManager() {
        return (Employee)getValue(Property.MANAGER);
    }

    public void setManager(Employee manager) {
        setValue(Property.MANAGER, manager);
    }

    @SuppressWarnings("unchecked")
    public Set<Employee> getDirectReports() {
        return (Set<Employee>)getValue(Property.DIRECT_REPORTS);
    }

    public void setDirectReports(Set<Employee> directReports) {
        setValue(Property.DIRECT_REPORTS, directReports);
    }

    public void addDirectReport(Employee directReport) {
        getDirectReports().add(directReport);
    }
    public Address getAddress() {
        return (Address)getValue(Property.ADDRESS);
    }

    public void setAddress(Address address) {
        setValue(Property.ADDRESS, address);
    }

}
