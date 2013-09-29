package com.nexusdata.modelgen.metamodel;

public class Relationship extends Property {
    String destinationEntity;
    String inverseName;
    boolean toMany;

    public String getJavaType() {
        if (toMany) {
            return "Set<" + destinationEntity + ">";
        } else {
            return destinationEntity;
        }
    }
}
