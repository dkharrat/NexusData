package org.nexusdata.metamodel;


// TODO: make it use generic type <T>?
public abstract class PropertyDescription {

    private final EntityDescription<?> m_entity;
    private final String m_name;
    private final Class<?> m_type;      //TODO: should this be moved to AttributeDescription, as it doesn't really make sense for relationships

    public PropertyDescription(EntityDescription<?> entity, String name, Class<?> type) {
        m_entity = entity;
        m_name = name;
        m_type = type;
    }

    public EntityDescription<?> getEntity() {
        return m_entity;
    }

    public Class<?> getType() {
        return m_type;
    }

    public String getName() {
        return m_name;
    }

    public abstract boolean isRelationship();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_entity == null) ? 0 : m_entity.hashCode());
        result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
        result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
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
        if (m_entity == null) {
            if (other.m_entity != null)
                return false;
        } else if (!m_entity.equals(other.m_entity))
            return false;
        if (m_name == null) {
            if (other.m_name != null)
                return false;
        } else if (!m_name.equals(other.m_name))
            return false;
        if (m_type == null) {
            if (other.m_type != null)
                return false;
        } else if (!m_type.equals(other.m_type))
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
