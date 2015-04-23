// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package com.github.dkharrat.nexusdata.test;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _Employee extends ManagedObject {

    public interface Property {
        String ID = "id";
        String FIRST_NAME = "firstName";
        String LAST_NAME = "lastName";
        String HOURLY_WAGE = "hourlyWage";
        String HEIGHT_IN_CM = "heightInCm";
        String ACTIVE = "active";
        String DATE_OF_BIRTH = "dateOfBirth";
        String COMPANY = "company";
        String MANAGER = "manager";
        String DIRECT_REPORTS = "directReports";
        String PASSPORT = "passport";
        String ADDRESS = "address";
    }


    public int getId() {
        return (Integer)getValue(Property.ID);
    }

    public void setId(int id) {
        setValue(Property.ID, id);
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

    public double getHourlyWage() {
        return (Double)getValue(Property.HOURLY_WAGE);
    }

    public void setHourlyWage(double hourlyWage) {
        setValue(Property.HOURLY_WAGE, hourlyWage);
    }

    public Float getHeightInCm() {
        return (Float)getValue(Property.HEIGHT_IN_CM);
    }

    public void setHeightInCm(Float heightInCm) {
        setValue(Property.HEIGHT_IN_CM, heightInCm);
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
    public Passport getPassport() {
        return (Passport)getValue(Property.PASSPORT);
    }

    public void setPassport(Passport passport) {
        setValue(Property.PASSPORT, passport);
    }

    public Address getAddress() {
        return (Address)getValue(Property.ADDRESS);
    }

    public void setAddress(Address address) {
        setValue(Property.ADDRESS, address);
    }

}
