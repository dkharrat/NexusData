package org.nexusdata.core;

import java.net.URI;

import org.nexusdata.metamodel.Entity;

//TODO: should this be parameterized on Entity type?

public class ObjectID {
    private final PersistentStore store;
    private final Entity<?> entity;
    private final Object id;

    ObjectID(PersistentStore store, Entity<?> entity, Object referenceObject) {
        this.store = store;
        this.entity = entity;
        id = referenceObject;
    }

    public PersistentStore getPersistentStore() {
        return store;
    }

    Class<?> getType() {
        return entity.getType();
    }

    public Entity<?> getEntity() {
        return entity;
    }

    Object getReferenceObject() {
        return id;
    }

    public boolean isTemporary() {
        return store == null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entity == null)   ? 0 : entity.hashCode());
        result = prime * result + ((id == null)       ? 0 : id.hashCode());
        result = prime * result + ((store == null)    ? 0 : store.hashCode());
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
        if (entity == null) {
            if (other.entity != null)
                return false;
        } else if (!entity.equals(other.entity))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (store == null) {
            if (other.store != null)
                return false;
        } else if (!store.equals(other.store))
            return false;
        return true;
    }

    public URI getUriRepresentation() {
        StringBuilder sb = new StringBuilder("nexusdata://");
        if (store != null) {
            sb.append(store.getUuid());
        }
        sb.append("/").append(entity.getName()).append("/").append(id.toString());
        return URI.create(sb.toString());
    }

    @Override
    public String toString() {
        return "<" + getUriRepresentation() + ">";
    }
}