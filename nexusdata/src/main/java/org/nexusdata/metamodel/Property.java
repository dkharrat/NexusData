package org.nexusdata.metamodel;


// TODO: make it use generic type <T>?

/**
 * A Property is an abstract representation of an object's field metadata. There are two kinds of properties:
 * {@link Attribute}s and {@link Relationship}s. These classes provide support for reflection on an ManagedObject.
 *
 * Each property is associated with a specific {@link Entity} and has a name. A property can also have certain
 * constraints, such as whether it's required or not.
 */
public abstract class Property {

    private final Entity<?> entity;
    private final String name;
    private final Class<?> type;      //TODO: should this be moved to Attribute, as it doesn't really make sense for relationships
    private final boolean isRequired; //TODO: what does this mean for a to-many relationship?

    /**
     * Constructs a new property.
     *
     * @param entity        the associated entity
     * @param name          the name of the property
     * @param type          the property type
     * @param isRequired    if true, property is required to have a value
     */
    public Property(Entity<?> entity, String name, Class<?> type, boolean isRequired) {
        this.entity = entity;
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
    }

    /**
     * Returns the associated entity of this property.
     *
     * @return the associated entity of this property
     */
    public Entity<?> getEntity() {
        return entity;
    }

    /**
     * Returns the type of this property.
     *
     * @return the type of this property
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the name this property.
     *
     * @return the name this property
     */
    public String getName() {
        return name;
    }

    /**
     * Indicates whether this property is required to have a value or not.
     *
     * @return true if this property is required to have a value, or false otherwise
     */
    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Indicates whether this property represents a relationship.
     *
     * @return true if this property represents a relationship, or false otherwise
     */
    public abstract boolean isRelationship();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entity == null) ? 0 : entity.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Property other = (Property) obj;
        if (entity == null) {
            if (other.entity != null)
                return false;
        } else if (!entity.equals(other.entity))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "["
                +   "name=" + getName()
                + ", type=" + getType().getName()
                + ", entity=" + getEntity().getName()
                + "]";
    }
}
