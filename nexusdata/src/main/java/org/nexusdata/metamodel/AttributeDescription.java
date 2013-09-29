package org.nexusdata.metamodel;


public class AttributeDescription extends PropertyDescription {

    public AttributeDescription(EntityDescription<?> entity, String name, Class<?> type) {
        super(entity, name, type);
    }

    @Override
    public boolean isRelationship() {
        return false;
    }
}
