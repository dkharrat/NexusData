package org.nexusdata.metamodel;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nexusdata.core.ManagedObject;

public class ObjectModel {
    private final int version;
    private final String name;
    private final Map<String,Entity<?>> entities = new HashMap<String,Entity<?>>();

    public ObjectModel(String name, List<Entity<?>> entities, int version) {
        this.name = name;
        this.version = version;
        initEntities(entities);
    }

    public ObjectModel(InputStream modelData) throws IOException {
        ObjectModelJsonParser.ParsedModel parsedModel = ObjectModelJsonParser.parseJsonModel(this, modelData);
        name = parsedModel.getName();
        version = parsedModel.getVersion();
        initEntities(parsedModel.getEntities());
    }

    private void initEntities(List<Entity<?>> entities) {
        for (Entity<?> entity : entities) {
            this.entities.put(entity.getName(), entity);
        }
    }

    public int getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public List<Entity<?>> getEntities() {
        return new ArrayList<Entity<?>>(entities.values());
    }

    @SuppressWarnings("unchecked")
    public <T extends ManagedObject> Entity<T> getEntity(Class<T> type) {
        return (Entity<T>) getEntity(type.getSimpleName());
    }

    public Entity<?> getEntity(String name) {
        return entities.get(name);
    }
}
