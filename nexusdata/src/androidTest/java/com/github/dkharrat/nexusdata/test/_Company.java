// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package com.github.dkharrat.nexusdata.test;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _Company extends ManagedObject {

    public interface Property {
        String NAME = "name";
        String EMPLOYEES = "employees";
        String DIRECTORS = "directors";
    }


    public String getName() {
        return (String)getValue(Property.NAME);
    }

    public void setName(String name) {
        setValue(Property.NAME, name);
    }


    @SuppressWarnings("unchecked")
    public Set<Employee> getEmployees() {
        return (Set<Employee>)getValue(Property.EMPLOYEES);
    }

    public void setEmployees(Set<Employee> employees) {
        setValue(Property.EMPLOYEES, employees);
    }

    public void addEmployee(Employee employee) {
        getEmployees().add(employee);
    }
    @SuppressWarnings("unchecked")
    public Set<Director> getDirectors() {
        return (Set<Director>)getValue(Property.DIRECTORS);
    }

    public void setDirectors(Set<Director> directors) {
        setValue(Property.DIRECTORS, directors);
    }

    public void addDirector(Director director) {
        getDirectors().add(director);
    }
}
