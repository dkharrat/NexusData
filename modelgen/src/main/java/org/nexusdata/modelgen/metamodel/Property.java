package org.nexusdata.modelgen.metamodel;

import com.google.common.base.CaseFormat;
import org.modeshape.common.text.Inflector;

public abstract class Property {

    String name;
    boolean required = false;
    boolean hasGetter = true;
    boolean hasSetter = true;

    public String getName() {
        return name;
    }

    abstract String getJavaType();

    static protected String capitalizedFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public String getCapitalizedName() {
        if (name == null || name.isEmpty()) {
            throw new RuntimeException("Invalid attribute name");
        }
        return capitalizedFirstChar(name);
    }

    public String getNameAsConstant() {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
    }

    public boolean isHasGetter() {
        return hasGetter;
    }

    public boolean isHasSetter() {
        return hasSetter;
    }

    public String getSingularName() {
        return Inflector.getInstance().singularize(name);
    }

    public String getMethodNameForGetter() {
        return "get" + getCapitalizedName();
    }

    public String getMethodNameForSetter() {
        return "set" + getCapitalizedName();
    }
}
