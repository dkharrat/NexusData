package com.github.dkharrat.nexusdata.test;

import java.util.List;

public class Company extends _Company {

    public Company() {
    }

    public void setEmployees(List<Employee> employees) {
        setValue(Property.EMPLOYEES, employees);
    }
}
