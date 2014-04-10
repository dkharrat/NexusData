package com.github.dkharrat.nexusdata.metamodel;

import com.github.dkharrat.nexusdata.core.ManagedObject;

/**
 * A Relationship is a {@link ManagedObject} {@link Property} that represents a field that stores a to-one or to-many
 * relationship.
 */
public class Relationship extends Property {
    /**
     * Represents the type of the relationship
     */
    public enum Type {
        TO_ONE,
        TO_MANY,
    }

    private final Type relationshipType;
    private final Entity<?> destinationEntity;
    private Relationship inverse;

    //TODO: look into deducing the 'type' param based on relationshipType
    /**
     * Creates a new Attribute.
     *
     * @param entity            the associated entity
     * @param name              the name of the property
     * @param type              the relationship instance type. For to-one relationships, it must be the same as
     *                          {@code destinationEntity} (i.e. type of the related object. For to-many relationships,
     *                          it must be {@link java.util.Set}.
     * @param relationshipType  the relationship type (to-one or to-many)
     * @param destinationEntity the entity of the object(s) this relationship will store
     * @param inverse           the relationship in the other direction (from the destination entity to this
     *                          relationship's entity
     * @param isRequired        if true, property is required to have a value
     */
    public Relationship(Entity<?> entity, String name, Class<?> type, Type relationshipType, Entity<?> destinationEntity, Relationship inverse, boolean isRequired) {
        super(entity, name, type, isRequired);
        this.relationshipType = relationshipType;
        this.destinationEntity = destinationEntity;
        this.inverse = inverse;
    }

    /**
     * Returns the relationship type.
     *
     * @return the relationship type
     */
    public Type getRelationshipType() {
        return relationshipType;
    }

    /**
     * Indicates whether this relationship is a to-one relationship or not.
     *
     * @return true if this relationship is a to-one relationship, or false otherwise
     */
    public boolean isToOne() {
        return relationshipType == Type.TO_ONE;
    }

    /**
     * Indicates whether this relationship is a to-many relationship or not.
     *
     * @return true if this relationship is a to-many relationship, or false otherwise
     */
    public boolean isToMany() {
        return relationshipType == Type.TO_MANY;
    }

    /**
     * Returns the entity to which this relationship is related to.
     *
     * @return the entity to which this relationship is related to
     */
    public Entity<?> getDestinationEntity() {
        return destinationEntity;
    }

    /**
     * Returns the inverse relationship (i.e the relationship in the other direction).
     *
     * @return the inverse relationship
     */
    public Relationship getInverse() {
        return inverse;
    }

    void setInverse(Relationship inverse) {
        this.inverse = inverse;
    }

    @Override
    public boolean isRelationship() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "["
                +   "destinationEntity=" + (getDestinationEntity() == null ? "<null>" : getDestinationEntity().getName())
                + ", inverseRelationship=" + (getInverse() == null ? "<null>" : getInverse().getName())
                + "]";
    }
}
