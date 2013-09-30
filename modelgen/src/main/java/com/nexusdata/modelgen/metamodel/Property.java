package org.nexusdata.modelgen.metamodel;

import com.google.common.base.CaseFormat;

public abstract class Property {

    String name;
    boolean hasGetter = true;
    boolean hasSetter = true;

    public String getName() {
        return name;
    }

    abstract String getJavaType();

    public String getCapitalizedName() {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Invalid attribute name");
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String getAllCapsName() {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
    }

    public boolean isHasGetter() {
        return hasGetter;
    }

    public boolean isHasSetter() {
        return hasSetter;
    }
}
