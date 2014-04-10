package com.github.dkharrat.nexusdata.core;

import java.net.URI;

import com.github.dkharrat.nexusdata.metamodel.Entity;

/**
 * An ObjectID represents a global identifier that uniquely identifies a {@link ManagedObject}. The identifier is
 * persisted across multiple runs of the application for the same ManagedObject. Moreover, two different instances of a
 * ManagedObject referring to the same backing record in the persistence store will have the same unique ObjectID.
 */
public class ObjectID {
    private final PersistentStore store;
    private final Entity<?> entity;
    private final Object id;

    ObjectID(PersistentStore store, Entity<?> entity, Object referenceObject) {
        this.store = store;
        this.entity = entity;
        id = referenceObject;
    }

    /**
     * Returns the persistence store associated with this ObjectID.
     *
     * @return the persistence store associated with this ObjectID
     */
    public PersistentStore getPersistentStore() {
        return store;
    }

    Class<?> getType() {
        return entity.getType();
    }

    /**
     * Returns the corresponding entity of this ObjectID.
     *
     * @return the corresponding entity of this ObjectID
     */
    public Entity<?> getEntity() {
        return entity;
    }

    Object getReferenceObject() {
        return id;
    }

    /**
     * Indicates whether this ObjectID is a temporary one and can change at some point in the future. When inserting a
     * new {@link ManagedObject} into an ObjectContext, it is assigned a temporary ID until it is saved, after which it
     * is allocated a unique permanent ID by the persistence store. When the ObjectID is temporary, do not keep a
     * permanent reference to it, since it is not guaranteed to stay the same. If you need a permanent ID assigned for
     * an object, you can request one from {@link ObjectContext#obtainPermanentIDsForObjects(java.util.Collection)}.
     *
     * @return true if this ObjectID is a temporary one, or false otherwise
     */
    public boolean isTemporary() {
        return store == null;
    }

    /**
     * Returns a URI-representation of this ObjectID, which can be used for persistence purposes. If the ObjectID is
     * temporary, this representation can change after the corresponding ManagedObject is saved. Persisted objects or
     * objects that have a permanent ID are guaranteed to return the same representation.
     *
     * <code>nexusdata://&lt;&gt;</code>
     *
     * @return a URI-representation of this ObjectID
     */
    public URI getUriRepresentation() {
        StringBuilder sb = new StringBuilder("nexusdata://");
        if (store != null) {
            sb.append(store.getUuid());
        }
        sb.append("/").append(entity.getName()).append("/").append(id.toString());
        return URI.create(sb.toString());
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

    /**
     * Determines whether this ObjectID is equivalent to another ObjectID.
     *
     * @param obj   the other object to compare against. If a non-ObjectID is passed, this method returns false.
     *
     * @return true if this ObjectID is equivalent to the other specified ObjectID, or false otherwise
     */
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

    @Override
    public String toString() {
        return "<" + getUriRepresentation() + ">";
    }
}
