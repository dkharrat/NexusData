package org.nexusdata.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nexusdata.core.ManagedObject;
import org.nexusdata.core.NoSuchPropertyException;

public class Entity<T extends ManagedObject> {

    private final ObjectModel model;
    private final Class<T> type;
    private final Map<String, Property> properties = new HashMap<String,Property>();

    public Entity(ObjectModel model, Class<T> type) {
        this.model = model;
        this.type = type;
    }

    public ObjectModel getModel() {
        return model;
    }

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

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public Collection<Property> getProperties() {
        return properties.values();
    }

    public Collection<Attribute> getAttributes() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (Property property : getProperties()) {
            if (property instanceof Attribute) {
                attributes.add((Attribute)property);
            }
        }

        return attributes;
    }

    public Collection<Relationship> getRelationships() {
        List<Relationship> relationships = new ArrayList<Relationship>();
        for (Property property : getProperties()) {
            if (property instanceof Relationship) {
                relationships.add((Relationship)property);
            }
        }

        return relationships;
    }

    public Property getProperty(String name) {
        Property property = properties.get(name);

        if (property == null) {
            throw new NoSuchPropertyException(this, name);
        }

        return property;
    }

    public Relationship getRelationship(String name) {
        Property property = getProperty(name);
        if (property instanceof Relationship) {
            return (Relationship) property;
        }

        throw new IllegalArgumentException("Property '"+name+"' is not a relationship.");
    }

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
