// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package com.github.dkharrat.nexusdata.test;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _Employee extends Person {

    public interface Property {
        String HOURLY_WAGE = "hourlyWage";
        String ACTIVE = "active";
        String COMPANY = "company";
        String MANAGER = "manager";
        String DIRECT_REPORTS = "directReports";
        String PASSPORT = "passport";
        String ADDRESS = "address";
    }


    public double getHourlyWage() {
        return (Double)getValue(Property.HOURLY_WAGE);
    }

    public void setHourlyWage(double hourlyWage) {
        setValue(Property.HOURLY_WAGE, hourlyWage);
    }

    public boolean isActive() {
        return (Boolean)getValue(Property.ACTIVE);
    }

    public void setActive(boolean active) {
        setValue(Property.ACTIVE, active);
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
