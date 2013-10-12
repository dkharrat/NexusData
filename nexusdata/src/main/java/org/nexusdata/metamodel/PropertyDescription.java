package org.nexusdata.metamodel;


// TODO: make it use generic type <T>?
public abstract class PropertyDescription {

    private final EntityDescription<?> entity;
    private final String name;
    private final Class<?> type;      //TODO: should this be moved to AttributeDescription, as it doesn't really make sense for relationships
    private final boolean isRequired;

    public PropertyDescription(EntityDescription<?> entity, String name, Class<?> type, boolean isRequired) {
        this.entity = entity;
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
    }

    public EntityDescription<?> getEntity() {
        return entity;
    }

    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return isRequired;
    }

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
        PropertyDescription other = (PropertyDescription) obj;
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
