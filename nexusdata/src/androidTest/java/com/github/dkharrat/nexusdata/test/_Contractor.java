// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package com.github.dkharrat.nexusdata.test;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _Contractor extends Employee {

    public interface Property {
        String FIRM_NAME = "firmName";
    }


    public String getFirmName() {
        return (String)getValue(Property.FIRM_NAME);
    }

    public void setFirmName(String firmName) {
        setValue(Property.FIRM_NAME, firmName);
    }


}
