package org.nexusdata.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nexusdata.core.ManagedObject;
import org.nexusdata.core.NoSuchPropertyException;

/**
 * An Entity represents the {@link ManagedObject}'s metadata. It is analogous to a Java's class. An instance of an
 * Entity is of supertype {@code ManagedObject}. An entity is composed of multiple properties, which are analogous to a
 * class' member fields.
 */
public class Entity<T extends ManagedObject> {

    private final ObjectModel model;
    private final Class<T> type;
    private final Map<String, Property> properties = new HashMap<String,Property>();

    /**
     * Creates a new Entity.
     *
     * @param model the model which this entity belongs to
     * @param type  the instance type of this entity
     */
    public Entity(ObjectModel model, Class<T> type) {
        this.model = model;
        this.type = type;
    }

    /**
     * Returns the model which this entity belongs to.
     *
     * @return the model which this entity belongs to
     */
    public ObjectModel getModel() {
        return model;
    }

    /**
     * Returns the instance type of this entity, which is what will be instantiated when a new instance of this entity
     * created.
     *
     * @return the instance type of this entity
     */
    public Class<T> getType() {
        return type;
    }

    void addProperty(Property property) {
        if (properties.containsKey(property.getName())) {
            throw new IllegalArgumentException(property + " already exists in entity " + getName());
        }
        properties.put(property.getName(), property);
    }

    void removeProperty(String name) {
        properties.remove(name);
    }

    /**
     * Indicates whether this entity defined the specified property name.
     *
     * @param name  the name of the property to check
     *
     * @return true if the specified property name is part of this entity, or false otherwise
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Returns the set of properties (attributes and relationships) defined on this entity.
     *
     * @return the set of properties defined on this entity
     */
    public Collection<Property> getProperties() {
        return properties.values();
    }

    /**
     * Returns the set of attributes defined on this entity.
     *
     * @return the set of attributes defined on this entity
     */
    public Collection<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (Property property : getProperties()) {
            if (property instanceof Attribute) {
                attributes.add((Attribute)property);
            }
        }

        return attributes;
    }

    /**
     * Returns the set of relationships defined on this entity.
     *
     * @return the set of relationships defined on this entity
     */
    public Collection<Relationship> getRelationships() {
        List<Relationship> relationships = new ArrayList<Relationship>();
        for (Property property : getProperties()) {
            if (property instanceof Relationship) {
                relationships.add((Relationship)property);
            }
        }

        return relationships;
    }

    /**
     * Returns the property instance from the specified name. The property must exist, or an exception is thrown.
     *
     * @param name  the name of the property to return
     *
     * @return the property for the specified name
     * @throws {@link NoSuchPropertyException} if no such property exists
     */
    public Property getProperty(String name) {
        Property property = properties.get(name);

        if (property == null) {
            throw new NoSuchPropertyException(this, name);
        }

        return property;
    }

    /**
     * Returns the attribute instance from the specified name. The attribute must exist, or an exception is thrown.
     *
     * @param name  the name of the attribute to return
     *
     * @return the attribute for the specified name
     * @throws {@link NoSuchPropertyException} if no such attribute exists
     *         {@link IllegalArgumentException} if the specified name is not an attribute
     */
    public Attribute getAttribute(String name) {
        Property property = getProperty(name);
        if (property instanceof Attribute) {
            return (Attribute) property;
        }

        throw new IllegalArgumentException("Property '"+name+"' is not an attribute.");
    }

    /**
     * Returns the relationship instance from the specified name. The relationship must exist, or an exception is thrown.
     *
     * @param name  the name of the relationship to return
     *
     * @return the relationship for the specified name
     * @throws {@link NoSuchPropertyException} if no such relationship exists
     *         {@link IllegalArgumentException} if the specified name is not a relationship
     */
    public Relationship getRelationship(String name) {
        Property property = getProperty(name);
        if (property instanceof Relationship) {
            return (Relationship) property;
        }

        throw new IllegalArgumentException("Property '"+name+"' is not a relationship.");
    }

    /**
     * Returns the name of this entity.
     *
     * @return the name of this entity
     */
    public String getName() {
        return type.getSimpleName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Entity<?> other = (Entity<?>) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Entity ["
                +   "name=" + getName()
                + ", class=" + getType().getName()
                + ", properties=" + properties.values()
                + "]";
    }
}
