package org.nexusdata.metamodel;


public class Attribute extends Property {

    public Object defaultValue;

    public Attribute(Entity<?> entity, String name, Class<?> type, boolean isRequired, Object defaultValue) {
        super(entity, name, type, isRequired);
        if (defaultValue != null && !getType().isAssignableFrom(defaultValue.getClass())) {
            throw new IllegalArgumentException("Type of defaultValue '" + defaultValue + "' is not compatible with type of this attribute (" + getType() + ")");
        }
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isRelationship() {
        return false;
    }
}
