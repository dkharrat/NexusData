package org.nexusdata.metamodel;


public class Relationship extends Property {
    public enum Type {
        TO_ONE,
        TO_MANY,
    }

    private final Type relationshipType;
    private final Entity<?> destinationEntity;
    private Relationship inverse;

    public Relationship(Entity<?> entity, String name, Class<?> type, Type relationshipType, Entity<?> destinationEntity, Relationship inverse, boolean isRequired) {
        super(entity, name, type, isRequired);
        this.relationshipType = relationshipType;
        this.destinationEntity = destinationEntity;
        this.inverse = inverse;
    }

    public Type getRelationshipType() {
        return relationshipType;
    }

    public boolean isToOne() {
        return relationshipType == Type.TO_ONE;
    }

    public boolean isToMany() {
        return relationshipType == Type.TO_MANY;
    }

    public Entity<?> getDestinationEntity() {
        return destinationEntity;
    }

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
                +   "destinationEntity=" + getDestinationEntity().getName()
                + ", inverseRelationship=" + getInverse().getName() +
                "]";
    }
}
