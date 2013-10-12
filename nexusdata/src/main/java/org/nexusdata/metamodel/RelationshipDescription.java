package org.nexusdata.metamodel;


public class RelationshipDescription extends PropertyDescription {
    public enum Type {
        TO_ONE,
        TO_MANY,
    }

    private final Type relationshipType;
    private final EntityDescription<?> destinationEntity;
    private RelationshipDescription inverse;

    public RelationshipDescription(EntityDescription<?> entity, String name, Class<?> type, Type relationshipType, EntityDescription<?> destinationEntity, RelationshipDescription inverse, boolean isRequired) {
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

    public EntityDescription<?> getDestinationEntity() {
        return destinationEntity;
    }

    public RelationshipDescription getInverse() {
        return inverse;
    }

    void setInverse(RelationshipDescription inverse) {
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
