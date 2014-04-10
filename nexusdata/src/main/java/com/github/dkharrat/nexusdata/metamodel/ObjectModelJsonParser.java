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

    static ParsedModel parseJsonModel(ObjectModel model, InputStream modelData) throws IOException {
        LOG.debug("Parsing model from stream");
        InputStreamReader reader = new InputStreamReader(modelData);

        Gson gson = new Gson();
        JsonObject rootJson = gson.fromJson(reader, JsonObject.class);
        JsonObject modelJsonObj = rootJson.get("model").getAsJsonObject();
        JsonElem.Model jsonModel = gson.fromJson(modelJsonObj, JsonElem.Model.class);
        int modelVersion = jsonModel.version;

        HashMap<String, Entity<?>> entities = setupEntities(jsonModel, model);

        // Setup mapping between entity and relationship info
        Map<Entity<?>, List<JsonElem.Relationship>> entityRelationMap = setupEntityRelationshipMapping(jsonModel, entities);

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

    static private HashMap<String, Entity<?>> setupEntities(JsonElem.Model jsonModel, ObjectModel model) {
        HashMap<String, Entity<?>> entities = new HashMap<String, Entity<?>>();

        for (JsonElem.Entity jsonEntity : jsonModel.entities) {
            LOG.debug("Creating entity {}", jsonEntity.name);

            @SuppressWarnings("unchecked")
            Class<ManagedObject> entityType = (Class<ManagedObject>)getEntityType(jsonModel.packageName, jsonEntity.name);
            Entity<ManagedObject> entity = new Entity<ManagedObject>(model, entityType);

            setupAttributes(jsonModel, jsonEntity, entity);

            entities.put(jsonEntity.name, entity);
        }

        return entities;
    }

    static private Map<Entity<?>, List<JsonElem.Relationship>> setupEntityRelationshipMapping(JsonElem.Model jsonModel, final HashMap<String, Entity<?>> entities) {
        Map<Entity<?>, List<JsonElem.Relationship>> entityRelationMap = new HashMap<Entity<?>, List<JsonElem.Relationship>>();

        for (JsonElem.Entity jsonEntity : jsonModel.entities) {
            for (JsonElem.Relationship jsonRelation : jsonEntity.relationships) {
                Entity<?> entity = entities.get(jsonEntity.name);
                List<JsonElem.Relationship> jsonRelations = entityRelationMap.get(entity);
                if (jsonRelations == null) {
                    jsonRelations = new ArrayList<JsonElem.Relationship>();
                    entityRelationMap.put(entity, jsonRelations);
                }
                jsonRelations.add(jsonRelation);
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
    }

    static private void setupRelationships(final Map<Entity<?>, List<JsonElem.Relationship>> entityRelationMap,
                                           final HashMap<String, Entity<?>> entities) {
        for (Map.Entry<Entity<?>, List<JsonElem.Relationship>> entityRelationPair : entityRelationMap.entrySet()) {
            Entity<?> entity = entityRelationPair.getKey();
            for (JsonElem.Relationship jsonRelation : entityRelationPair.getValue()) {
                Entity<?> destinationEntity = entities.get(jsonRelation.destinationEntity);
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
                // TODO: pluralize inverseName for to-many relationship by default
                String inverseName = entity.getName().toLowerCase(Locale.ENGLISH);

                if (!StringUtil.isBlank(jsonRelation.inverseName)) {
                    inverseName = jsonRelation.inverseName;
                }

                try {
                    Relationship inverseRelationship = (Relationship) destinationEntity.getProperty(inverseName);
                    relationship.setInverse(inverseRelationship);
                } catch (NoSuchPropertyException e) {
                    throw new RuntimeException("Could not find inverse property " + inverseName + " in destination entity " + destinationEntity.getName() + " for relationship " + relationship.getName(), e);
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
        List<Entity> entities;
    }

    static class Entity {
        String name;
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
            return toMany ? com.github.dkharrat.nexusdata.metamodel.Relationship.Type.TO_MANY : com.github.dkharrat.nexusdata.metamodel.Relationship.Type.TO_ONE;
        }
    }
}
