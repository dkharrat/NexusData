package org.nexusdata.metamodel;

import java.io.*;
import java.util.*;

import org.nexusdata.core.ManagedObject;

/**
 * An ObjectModel represents the collection of {@link Entity}s that defines the types of objects that exist in a
 * persistence store.
 */
public class ObjectModel {
    private final int version;
    private final String name;
    private final Map<String,Entity<?>> entities = new HashMap<String,Entity<?>>();

    /**
     * Creates a new object model.
     *
     * @param name      the name of the model
     * @param entities  the set of entities defined in this model
     * @param version   the version of this model
     */
    public ObjectModel(String name, Collection<Entity<?>> entities, int version) {
        this.name = name;
        this.version = version;
        initEntities(entities);
    }

    /**
     * Creates a new ObjectModel from a model file
     *
     * @param modelData     the input stream for the model file
     * @throws IOException  if there was a problem reading from the input stream
     */
    public ObjectModel(InputStream modelData) throws IOException {
        ObjectModelJsonParser.ParsedModel parsedModel = ObjectModelJsonParser.parseJsonModel(this, modelData);
        name = parsedModel.getName();
        version = parsedModel.getVersion();
        initEntities(parsedModel.getEntities());
    }

    private void initEntities(Collection<Entity<?>> entities) {
        for (Entity<?> entity : entities) {
            this.entities.put(entity.getName(), entity);
        }
    }

    /**
     * Returns the model version. Model versions are used to identify different versions of the same semantic model.
     * When making changes to a model that has already been published and is in use, make sure to increment the version
     * number to have persistence stores using older versions of the model to upgrade.
     *
     * @return  an integer representing the model's version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the model's name.
     *
     * @return the model's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the set of entities defined in this model.
     *
     * @return the set of entities defined in this model
     */
    public Collection<Entity<?>> getEntities() {
        return entities.values();
    }

    /**
     * Returns the entity in this model by the entity's class type.
     *
     * @param type  the class type
     *
     * @return  the entity corresponding to the class type
     */
    @SuppressWarnings("unchecked")
    public <T extends ManagedObject> Entity<T> getEntity(Class<T> type) {
        return (Entity<T>) getEntity(type.getSimpleName());
    }

    /**
     * Returns the entity in this model by the entity's name.
     *
     * @param name  the name of the entity
     *
     * @return  the entity corresponding to the class type
     */
    @SuppressWarnings("unchecked")
    public <T extends ManagedObject> Entity<T> getEntity(String name) {
        return (Entity<T>)entities.get(name);
    }
}
