package org.nexusdata.metamodel;


public class RelationshipDescription extends PropertyDescription {
    public enum Type {
        TO_ONE,
        TO_MANY,
    }

    private final Type m_relationshipType;
    private final EntityDescription<?> m_destinationEntity;
    private RelationshipDescription m_inverse;

    public RelationshipDescription(EntityDescription<?> entity, String name, Class<?> type, Type relationshipType, EntityDescription<?> destinationEntity, RelationshipDescription inverse, boolean isRequired) {
        super(entity, name, type, isRequired);
        m_relationshipType = relationshipType;
        m_destinationEntity = destinationEntity;
        m_inverse = inverse;
    }

    public Type getRelationshipType() {
        return m_relationshipType;
    }

    public boolean isToOne() {
        return m_relationshipType == Type.TO_ONE;
    }

    public boolean isToMany() {
        return m_relationshipType == Type.TO_MANY;
    }

    public EntityDescription<?> getDestinationEntity() {
        return m_destinationEntity;
    }

    public RelationshipDescription getInverse() {
        return m_inverse;
    }

    void setInverse(RelationshipDescription m_inverse) {
        this.m_inverse = m_inverse;
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
