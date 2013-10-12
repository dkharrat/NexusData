package org.nexusdata.metamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nexusdata.core.ManagedObject;

public class EntityDescription<T extends ManagedObject> {

    private final ObjectModel model;
    private final Class<T> type;
    private final Map<String, PropertyDescription> properties = new HashMap<String,PropertyDescription>();

    public EntityDescription(ObjectModel model, Class<T> type) {
        this.model = model;
        this.type = type;
    }

    public ObjectModel getModel() {
        return model;
    }

    public Class<T> getType() {
        return type;
    }

    void addProperty(PropertyDescription property) {
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

    public Collection<PropertyDescription> getProperties() {
        return properties.values();
    }

    public Collection<AttributeDescription> getAttributes() {
        List<AttributeDescription> attributes = new ArrayList<AttributeDescription>();
        for (PropertyDescription property : getProperties()) {
            if (property instanceof AttributeDescription) {
                attributes.add((AttributeDescription)property);
            }
        }

        return attributes;
    }

    public Collection<RelationshipDescription> getRelationships() {
        List<RelationshipDescription> relationships = new ArrayList<RelationshipDescription>();
        for (PropertyDescription property : getProperties()) {
            if (property instanceof RelationshipDescription) {
                relationships.add((RelationshipDescription)property);
            }
        }

        return relationships;
    }

    public PropertyDescription getProperty(String name) throws NoSuchFieldException {
        PropertyDescription property = properties.get(name);

        if (property == null) {
            throw new NoSuchFieldException("Could not find property named: " + name);
        }

        return property;
    }

    public RelationshipDescription getRelationship(String name) throws NoSuchFieldException {
        PropertyDescription property = getProperty(name);
        if (property instanceof RelationshipDescription) {
            return (RelationshipDescription) property;
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
        EntityDescription<?> other = (EntityDescription<?>) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EntityDescription ["
                +   "name=" + getName()
                + ", class=" + getType().getName()
                + ", properties=" + properties.values()
                + "]";
    }
}
