package org.nexusdata.modelgen.metamodel;

import java.util.List;

public class Model {
    String name;
    Integer version;
    String packageName;
    List<Entity> entities;

    public String getName() {
        return name;
    }

    public Integer getVersion() {
        return version;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<Entity> getEntities() {
        return entities;
    }
}
