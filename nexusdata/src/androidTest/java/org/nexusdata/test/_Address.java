// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package com.github.dkharrat.nexusdata.test;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _Address extends ManagedObject {

    public interface Property {
        final static String STREET_NAME = "streetName";
        final static String COUNTRY = "country";
        final static String EMPLOYEE = "employee";
    }


    public String getStreetName() {
        return (String)getValue(Property.STREET_NAME);
    }

    public void setStreetName(String streetName) {
        setValue(Property.STREET_NAME, streetName);
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
