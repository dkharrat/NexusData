// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package com.github.dkharrat.nexusdata.test;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _Passport extends ManagedObject {

    public interface Property {
        final static String NUMBER = "number";
        final static String COUNTRY = "country";
        final static String ISSUE_DATE = "issueDate";
        final static String EXPIRATION_DATE = "expirationDate";
        final static String EMPLOYEE = "employee";
    }


    public String getNumber() {
        return (String)getValue(Property.NUMBER);
    }

    public void setNumber(String number) {
        setValue(Property.NUMBER, number);
    }

    public String getCountry() {
        return (String)getValue(Property.COUNTRY);
    }

    public void setCountry(String country) {
        setValue(Property.COUNTRY, country);
    }

    public Date getIssueDate() {
        return (Date)getValue(Property.ISSUE_DATE);
    }

    public void setIssueDate(Date issueDate) {
        setValue(Property.ISSUE_DATE, issueDate);
    }

    public Date getExpirationDate() {
        return (Date)getValue(Property.EXPIRATION_DATE);
    }

    public void setExpirationDate(Date expirationDate) {
        setValue(Property.EXPIRATION_DATE, expirationDate);
    }


    public Employee getEmployee() {
        return (Employee)getValue(Property.EMPLOYEE);
    }

    public void setEmployee(Employee employee) {
        setValue(Property.EMPLOYEE, employee);
    }

}
