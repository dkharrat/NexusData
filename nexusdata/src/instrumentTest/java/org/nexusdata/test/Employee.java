package org.nexusdata.test;

import java.util.Comparator;

public class Employee extends _Employee {

    public Employee() {
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
