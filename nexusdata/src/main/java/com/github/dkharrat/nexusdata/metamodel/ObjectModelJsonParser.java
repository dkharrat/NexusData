package com.github.dkharrat.nexusdata.metamodel;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.github.dkharrat.nexusdata.core.ManagedObject;
import com.github.dkharrat.nexusdata.core.NoSuchPropertyException;
import com.github.dkharrat.nexusdata.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ObjectModelJsonParser {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectModelJsonParser.class);

    static class ParsedModel {
        private String name;
        private int version;
        private List<Entity<?>> entities;

        ParsedModel(String name, int version, List<Entity<?>> entities) {
            this.name = name;
            this.version = version;
            this.entities = entities;
        }

        String getName() {
            return name;
        }

        int getVersion() {
            return version;
        }

        List<Entity<?>> getEntities() {
            return entities;
        }
    }

    static ParsedModel parseJsonModel(ObjectModel model, InputStream modelData, String includePath) throws IOException {
        LOG.debug("Parsing model from stream");
        InputStreamReader reader = new InputStreamReader(modelData);

        Gson gson = new Gson();
        JsonObject rootJson = gson.fromJson(reader, JsonObject.class);
        JsonObject modelJsonObj = rootJson.get("model").getAsJsonObject();
        JsonElem.Model jsonModel = gson.fromJson(modelJsonObj, JsonElem.Model.class);
        int modelVersion = jsonModel.version;
        List<ObjectModel> includeModels = new ArrayList<>();
        if (!jsonModel.includeModels.isEmpty()) {
            for (String includeModelFilename : jsonModel.includeModels) {
                String filename = includePath + "/" + includeModelFilename;
                InputStream is = ObjectModel.class.getResourceAsStream(filename);
                if (is == null) {
                    throw new RuntimeException("Could not find file " + filename);
                }
                ObjectModel includeModel = new ObjectModel(is, includePath);
                includeModels.add(includeModel);
            }
        }

        HashMap<String, Entity<?>> entities = setupEntities(jsonModel, model, includeModels);

        // Setup mapping between entity and relationship info
        Map<Entity<?>, List<JsonElem.Relationship>> entityRelationMap = setupEntityRelationshipMapping(
                jsonModel,
                entities
        );

        setupRelationships(entityRelationMap, entities);

        Entity<?>[] entitiesArray = entities.values().toArray(new Entity<?>[0]);
        LOG.debug("Done parsing model");
        return new ParsedModel(jsonModel.name, modelVersion, Arrays.asList(entitiesArray));
    }

    static private Class<?> getEntityType(String packageName, String entityName) {
        Class<?> entityType = ManagedObject.class;
        String className = packageName + "." + entityName;
        try {
            entityType = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            LOG.warn("Could not find class {}. Will default to ManagedObject", className);
        }

        return entityType;
    }

    static private Class<?> getEnumType(String packageName, String entityName, String enumName) {
        try {
            return Class.forName(packageName + "._" + entityName + "$" + enumName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    static private HashMap<String, Entity<?>> setupEntities(
            JsonElem.Model jsonModel,
            ObjectModel model,
            List<ObjectModel> modelsToIncludeEntities)
    {
        HashMap<String, Entity<?>> entities = new HashMap<>();

        for (JsonElem.Entity jsonEntity : jsonModel.entities) {
            LOG.debug("Creating entity {}", jsonEntity.name);

            @SuppressWarnings("unchecked")
            Class<ManagedObject> entityType = (Class<ManagedObject>)getEntityType(jsonModel.packageName, jsonEntity.name);
            Entity<ManagedObject> entity = new Entity<>(model, entityType);

            entities.put(jsonEntity.name, entity);
        }

        for (ObjectModel includeModel : modelsToIncludeEntities) {
            for (Entity<?> entity : includeModel.getEntities()) {
                entities.put(entity.getName(), entity);
            }
        }

        // do another pass to setup entity inheritance and attributes
        for (JsonElem.Entity jsonEntity : jsonModel.entities) {
            Entity<?> entity = entities.get(jsonEntity.name);

            if (jsonEntity.superEntityName != null) {
                Entity<?> superEntity = entities.get(jsonEntity.superEntityName);
                if (superEntity == null) {
                    throw new RuntimeException("Could not find super entity " + jsonEntity.superEntityName);
                }
                entity.setSuperEntity(superEntity);
            }

            setupAttributes(jsonModel, jsonEntity, entity);
        }

        return entities;
    }

    static private Map<Entity<?>, List<JsonElem.Relationship>> setupEntityRelationshipMapping(
            JsonElem.Model jsonModel,
            final HashMap<String, Entity<?>> entities
    ) {
        Map<Entity<?>, List<JsonElem.Relationship>> entityRelationMap = new HashMap<Entity<?>, List<JsonElem.Relationship>>();

        for (JsonElem.Entity jsonEntity : jsonModel.entities) {
            if (jsonEntity.relationships != null) {
                for (JsonElem.Relationship jsonRelation : jsonEntity.relationships) {
                    Entity<?> entity = entities.get(jsonEntity.name);
                    List<JsonElem.Relationship> jsonRelations = entityRelationMap.get(entity);
                    if (jsonRelations == null) {
                        jsonRelations = new ArrayList<>();
                        entityRelationMap.put(entity, jsonRelations);
                    }
                    jsonRelations.add(jsonRelation);
                }
            }
        }

        return entityRelationMap;
    }

    static private void setupAttributes(JsonElem.Model jsonModel, JsonElem.Entity jsonEntity, Entity<?> entity) {
        for (JsonElem.Attribute jsonAttr : jsonEntity.attributes) {
            Class<?> attrType = JsonElem.Attribute.primTypeToJavaType.get(jsonAttr.type);
            if (attrType == null) {
                attrType = getEnumType(jsonModel.packageName, jsonEntity.name, jsonAttr.type);
                if (attrType == null) {
                    throw new RuntimeException("Unknown type '" + jsonAttr.type + "' for " + jsonEntity.name + "#" + jsonAttr.name);
                }
            }
            Attribute attr = new Attribute(entity, jsonAttr.name, attrType, jsonAttr.required, jsonAttr.getDefaultValue());
            LOG.debug("Adding attribute: " + jsonAttr.name);
            entity.addProperty(attr);
        }

        // add the attributes of the super-entity if set
        if (entity.getSuperEntity() != null) {
            for (Attribute attribute : entity.getSuperEntity().getAttributes()) {
                entity.addProperty(attribute);
            }
        }
    }

    static private void setupRelationships(
            final Map<Entity<?>, List<JsonElem.Relationship>> entityRelationMap,
            final HashMap<String, Entity<?>> entities)
    {
        for (Map.Entry<Entity<?>, List<JsonElem.Relationship>> entityRelationPair : entityRelationMap.entrySet()) {
            Entity<?> entity = entityRelationPair.getKey();
            for (JsonElem.Relationship jsonRelation : entityRelationPair.getValue()) {
                Entity<?> destinationEntity = entities.get(jsonRelation.destinationEntity);
                if (destinationEntity == null) {
                    throw new RuntimeException("Could not find destination entity " + jsonRelation.destinationEntity +
                            " for relationship '" + entity.getName() + "#" + jsonRelation.name + "'");
                }
                Relationship.Type relationshipType = jsonRelation.getRelationshipType();
                Class<?> relationClassType = (relationshipType == Relationship.Type.TO_MANY) ? Set.class : destinationEntity.getType();
                Relationship relationship = new Relationship(
                        entity,
                        jsonRelation.name,
                        relationClassType,
                        relationshipType,
                        destinationEntity,
                        null,
                        jsonRelation.required);
                LOG.debug("Adding relationship: " + jsonRelation.name + " for entity: " + entity.getName());
                entity.addProperty(relationship);
            }
        }

        // make another pass to setup the inverse relationships
        for (Map.Entry<Entity<?>, List<JsonElem.Relationship>> entityRelationPair : entityRelationMap.entrySet()) {
            Entity<?> entity = entityRelationPair.getKey();
            for (JsonElem.Relationship jsonRelation : entityRelationPair.getValue()) {
                Relationship relationship = entity.getRelationship(jsonRelation.name);

                Entity<?> destinationEntity = relationship.getDestinationEntity();

                String inverseName = jsonRelation.inverseName;

                if (inverseName != null) {
                    try {
                        Relationship inverseRelationship = (Relationship) destinationEntity.getProperty(inverseName);

                        relationship.setInverse(inverseRelationship);
                        if (!relationship.getEntity().getType().isAssignableFrom(inverseRelationship.getDestinationEntity().getType())) {
                            throw new RuntimeException("Incompatible types in relationship between entities: " +
                                    relationship.getEntity().getName() + "#" + relationship.getName() + " <--> " +
                                    inverseRelationship.getEntity().getName() + "#" + inverseRelationship.getName());
                        }
                    } catch (NoSuchPropertyException e) {
                        throw new RuntimeException("Could not find inverse property " + inverseName +
                                " in destination entity " + destinationEntity.getName() + " for relationship " +
                                relationship.getName(), e);
                    }
                }
            }
        }

        // for each entity, add the relationships of the super-entity if set
        for (Entity<?> entity : entities.values()) {
            if (entity.getSuperEntity() != null) {
                for (Relationship relationship : entity.getSuperEntity().getRelationships()) {
                    entity.addProperty(relationship);
                }
            }
        }
    }
}

class JsonElem {
    static class Model {
        String name;
        Integer version;
        String packageName;
        List<String> includeModels = new ArrayList<>();
        List<Entity> entities;
    }

    static class Entity {
        String name;
        @SerializedName("extends") String superEntityName;
        List<Attribute> attributes;
        List<Relationship> relationships;
        List<EnumProperty> enums;
    }

    static class EnumProperty {
        String name;
        List<String> values;
    }

    static class Property {
        boolean required;
    }


    static class Attribute extends Property {

        static final Map<String,Class<?>> primTypeToJavaType = new HashMap<String,Class<?>>();
        static
        {
            primTypeToJavaType.put("String", String.class);
            primTypeToJavaType.put("Int", Integer.class);
            primTypeToJavaType.put("Long", Long.class);
            primTypeToJavaType.put("Bool", Boolean.class);
            primTypeToJavaType.put("Date", Date.class);
            primTypeToJavaType.put("Float", Float.class);
            primTypeToJavaType.put("Double", Double.class);
        }

        String name;
        String type;
        @SerializedName("default") String defaultValue;
        boolean hasGetter = true;
        boolean hasSetter = true;

        Object getDefaultValue() {
            if (defaultValue == null) {
                return null;
            }

            try {
                return StringUtil.convertStringValueToType(defaultValue, primTypeToJavaType.get(type));
            } catch (ParseException ex) {
                throw new RuntimeException("Could not parse default value " + defaultValue + " for attribute " + name);
            }
        }
    }

    static class Relationship extends Property {
        String name;
        String destinationEntity;
        String inverseName;
        boolean toMany;

        com.github.dkharrat.nexusdata.metamodel.Relationship.Type getRelationshipType() {
            return toMany ?
                    com.github.dkharrat.nexusdata.metamodel.Relationship.Type.TO_MANY :
                    com.github.dkharrat.nexusdata.metamodel.Relationship.Type.TO_ONE;
        }
    }
}
