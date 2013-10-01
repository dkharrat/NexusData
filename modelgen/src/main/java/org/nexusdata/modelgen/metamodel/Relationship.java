package org.nexusdata.modelgen.metamodel;

import org.modeshape.common.text.Inflector;

public class Relationship extends Property {
    String destinationEntity;
    String inverseName;
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
        return "add" + Inflector.getInstance().capitalize(getSingularName());
    }
}
