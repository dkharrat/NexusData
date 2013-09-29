package com.nexusdata.modelgen.metamodel;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    String name;
    List<Attribute> attributes;
    List<Relationship> relationships;
    List<EnumProperty> enums;

    public Entity() {
        attributes = new ArrayList<>();
        relationships = new ArrayList<>();
        enums = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public List<Relationship> getRelationships() {
        return relationships;
    }

    public List<EnumProperty> getEnums() {
        return enums;
    }
}
