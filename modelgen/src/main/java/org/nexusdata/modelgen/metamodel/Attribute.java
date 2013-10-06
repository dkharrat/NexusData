package org.nexusdata.modelgen.metamodel;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class Attribute  extends Property {

    private static final Map<String,String> typeToJavaType = new HashMap<>();
    static
    {
        typeToJavaType.put("String", "String");
        typeToJavaType.put("Int", "Integer");
        typeToJavaType.put("Long", "Long");
        typeToJavaType.put("Bool", "Boolean");
        typeToJavaType.put("Date", "Date");
    }

    private static final Map<String,String> typeToPrimType = new HashMap<>();
    static
    {
        typeToPrimType.put("String", "String");
        typeToPrimType.put("Int", "int");
        typeToPrimType.put("Long", "long");
        typeToPrimType.put("Bool", "boolean");
        typeToPrimType.put("Date", "Date");
    }

    private Entity entity;
    private String type;
    @SerializedName("default") private String defaultValue;

    // use to set parent entity when object is de-serialized
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public String getJavaType() {
        return getJavaType(true);
    }

    public String getJavaTypeForParam() {
        return getJavaType(!required);
    }

    private String getJavaType(boolean useObjectType) {
        String javaType = useObjectType ? typeToJavaType.get(type): typeToPrimType.get(type);
        if (javaType == null) {
            if (isEnumProperty(type)) {
                javaType = type;
            }
        }
        if (javaType == null) {
            throw new RuntimeException("Unknown type '" + type + "' for attribute '" + name + "'");
        }
        return javaType;
    }

    private boolean isEnumProperty(String name) {
        for (EnumProperty enumProp : entity.getEnums()) {
            if (enumProp.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public String getMethodNameForGetter() {
        if (type.equals("Bool")) {
            if (name.substring(0, 2).equalsIgnoreCase("is")) {
                return name;
            } else {
                return "is" + getCapitalizedName();
            }
        } else {
            return super.getMethodNameForGetter();
        }
    }
}
