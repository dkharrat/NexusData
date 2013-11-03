package org.nexusdata.core;

import org.nexusdata.metamodel.Entity;

/**
 * This exception is thrown when a non-existent property is accessed.
 */
public class NoSuchPropertyException extends RuntimeException {
    private String propertyName;

    public NoSuchPropertyException(ManagedObject object, String propertyName) {
        super("No such property '" + propertyName + "' in object " + object);
        this.propertyName = propertyName;
    }

    public NoSuchPropertyException(Entity<?> entity, String propertyName) {
        super("No such property '" + propertyName + "' in entity " + entity);
        this.propertyName = propertyName;
    }

    /**
     * Returns the name of the property that was attempted to be accessed.
     * @return the name of the property that was attempted to be accessed
     */
    public String getPropertyName() {
        return propertyName;
    }
}
