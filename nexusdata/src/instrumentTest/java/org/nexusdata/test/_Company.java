// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package org.nexusdata.test;

import java.util.Date;
import java.util.Set;
import org.nexusdata.core.ManagedObject;

class _Company extends ManagedObject {

    public interface Property {
        final static String NAME = "name";
        final static String EMPLOYEES = "employees";
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
}
