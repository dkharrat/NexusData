package com.github.dkharrat.nexusdata.modelgen.metamodel;

public class Relationship extends Property {
    String destinationEntity;
    boolean toMany = false;

    public String getJavaType() {
        if (toMany) {
            return "Set<" + destinationEntity + ">";
        } else {
            return destinationEntity;
        }
    }

    public boolean isToMany() {
        return toMany;
    }

    public String getDestinationEntity() {
        return destinationEntity;
    }

    public String getMethodNameForAddingToCollection() {
        return "add" + capitalizedFirstChar(getSingularName());
    }
}
