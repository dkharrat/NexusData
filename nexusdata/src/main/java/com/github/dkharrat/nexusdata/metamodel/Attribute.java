package com.github.dkharrat.nexusdata.metamodel;

import com.github.dkharrat.nexusdata.core.ManagedObject;

/**
 * An Attribute is a {@link ManagedObject} {@link Property} that represents a field that stores a concrete value (e.g.
 * an Integer, String, Date, etc.).
 */
public class Attribute extends Property {

    private Object defaultValue;

    /**
     * Creates a new Attribute.
     *
     * @param entity        the associated entity
     * @param name          the name of the property
     * @param type          the property type
     * @param isRequired    if true, property is required to have a value
     * @param defaultValue  the default value to initialize the attribute when constructing a new {@link ManagedObject}
     */
    public Attribute(Entity<?> entity, String name, Class<?> type, boolean isRequired, Object defaultValue) {
        super(entity, name, type, isRequired);
        if (defaultValue != null && !getType().isAssignableFrom(defaultValue.getClass())) {
            throw new IllegalArgumentException("Type of defaultValue '" + defaultValue + "' is not compatible with type of this attribute (" + getType() + ")");
        }
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the default value for this attribute.
     *
     * @return the default value for this attribute
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean isRelationship() {
        return false;
    }
}
