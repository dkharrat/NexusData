package org.nexusdata.core;

import java.net.URI;

import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.metamodel.EntityDescription;


public class ObjectID {
    private final PersistentStore m_store;
    private final EntityDescription<?> m_entity;
    private final Object m_id;

    ObjectID(PersistentStore store, EntityDescription<?> entity, Object referenceObject) {
        m_store = store;
        m_entity = entity;
        m_id = referenceObject;
    }

    public PersistentStore getPersistentStore() {
        return m_store;
    }

    Class<?> getType() {
        return m_entity.getType();
    }

    public EntityDescription<?> getEntity() {
        return m_entity;
    }

    Object getReferenceObject() {
        return m_id;
    }

    public boolean isTemporary() {
        return m_store == null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_entity == null)   ? 0 : m_entity.hashCode());
        result = prime * result + ((m_id == null)       ? 0 : m_id.hashCode());
        result = prime * result + ((m_store == null)    ? 0 : m_store.hashCode());
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
        ObjectID other = (ObjectID) obj;
        if (m_entity == null) {
            if (other.m_entity != null)
                return false;
        } else if (!m_entity.equals(other.m_entity))
            return false;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        if (m_store == null) {
            if (other.m_store != null)
                return false;
        } else if (!m_store.equals(other.m_store))
            return false;
        return true;
    }

    public URI getUriRepresentation() {
        StringBuilder sb = new StringBuilder("nexusdata://");
        if (m_store != null) {
            sb.append(m_store.getUuid());
        }
        sb.append("/").append(m_entity.getName()).append("/").append(m_id.toString());
        return URI.create(sb.toString());
    }

    @Override
    public String toString() {
        return "<" + getUriRepresentation() + ">";
    }
}