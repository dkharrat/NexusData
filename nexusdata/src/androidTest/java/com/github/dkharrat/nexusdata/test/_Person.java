// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package com.github.dkharrat.nexusdata.test;

import java.util.Date;
import java.util.Set;
import com.github.dkharrat.nexusdata.core.ManagedObject;

abstract class _Person extends ManagedObject {

    public interface Property {
        String ID = "id";
        String FIRST_NAME = "firstName";
        String LAST_NAME = "lastName";
        String HEIGHT_IN_CM = "heightInCm";
        String DATE_OF_BIRTH = "dateOfBirth";
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

    public Float getHeightInCm() {
        return (Float)getValue(Property.HEIGHT_IN_CM);
    }

    public void setHeightInCm(Float heightInCm) {
        setValue(Property.HEIGHT_IN_CM, heightInCm);
    }

    public Date getDateOfBirth() {
        return (Date)getValue(Property.DATE_OF_BIRTH);
    }

    public void setDateOfBirth(Date dateOfBirth) {
        setValue(Property.DATE_OF_BIRTH, dateOfBirth);
    }


}
