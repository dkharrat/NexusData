// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package com.github.dkharrat.nexusdata.test;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _Director extends Person {

    public interface Property {
        String ACTIVE = "active";
        String COMPANY = "company";
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

}
