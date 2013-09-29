package org.nexusdata.metamodel;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.nexusdata.core.ManagedObject;
import org.nexusdata.metamodel.Attributes.Attribute;
import org.nexusdata.metamodel.RelationshipDescription.Type;
import org.nexusdata.metamodel.Relationships.Relationship;
import org.nexusdata.utils.StreamUtil;
import org.nexusdata.metamodel.ObjectModelJsonParser.ParsedModel;
import org.nexusdata.core.ManagedObject;

public class ObjectModel {
    private final int m_version;
    private final Map<String,EntityDescription<?>> m_entities = new HashMap<String,EntityDescription<?>>();

    public ObjectModel(List<EntityDescription<?>> entities, int version) {
        m_version = version;
        initEntities(entities);
    }

    public ObjectModel(InputStream modelData) throws IOException {
        ObjectModelJsonParser.ParsedModel parsedModel = ObjectModelJsonParser.parseJsonModel(this, modelData);
        m_version = parsedModel.getVersion();
        initEntities(parsedModel.getEntities());
    }

    private void initEntities(List<EntityDescription<?>> entities) {
        for (EntityDescription<?> entity : entities) {
            m_entities.put(entity.getName(), entity);
        }
    }

    public int getVersion() {
        return m_version;
    }

    public List<EntityDescription<?>> getEntities() {
        return new ArrayList<EntityDescription<?>>(m_entities.values());
    }

    @SuppressWarnings("unchecked")
    public <T extends ManagedObject> EntityDescription<T> getEntity(Class<T> type) {
        return (EntityDescription<T>) getEntity(type.getSimpleName());
    }

    public EntityDescription<?> getEntity(String name) {
        return m_entities.get(name);
    }

    private static List<Attributes.Attribute> getAllInheritedClassAttributes(final Class<?> type) {
        final List<Attributes.Attribute> fields = new ArrayList<Attributes.Attribute>();
        Class<?> klass = type;
        while (klass != ManagedObject.class) {
            Attributes attributes = klass.getAnnotation(Attributes.class);
            if (attributes != null) {
                fields.addAll(Arrays.asList(attributes.value()));
            }
            klass = klass.getSuperclass();
        }
        return fields;
    }

    private static List<Relationships.Relationship> getAllInheritedClassRelationships(final Class<?> type) {
        final List<Relationships.Relationship> fields = new ArrayList<Relationships.Relationship>();
        Class<?> klass = type;
        while (klass != ManagedObject.class) {
            Relationships relationships = klass.getAnnotation(Relationships.class);
            if (relationships != null) {
                fields.addAll(Arrays.asList(relationships.value()));
            }
            klass = klass.getSuperclass();
        }
        return fields;
    }

    private void setupEntities() {

        Map<RelationshipDescription,Relationships.Relationship> relationshipAnnotations = new HashMap<RelationshipDescription,Relationships.Relationship>();

        for (EntityDescription<?> entity : m_entities.values()) {
            List<Attributes.Attribute> attributes = getAllInheritedClassAttributes(entity.getType());
            List<Relationships.Relationship> relationships = getAllInheritedClassRelationships(entity.getType());

            for (Attributes.Attribute attributeAnnotation : attributes) {
                PropertyDescription property = null;
                String name = attributeAnnotation.name();
                Class<?> type = attributeAnnotation.type();

                property = new AttributeDescription(entity, name, type);
                entity.addProperty(property);
            }

            for (Relationships.Relationship relationshipAnnotation : relationships) {
                RelationshipDescription relationship = null;
                String name = relationshipAnnotation.name();
                Class<?> type = relationshipAnnotation.type();
                RelationshipDescription.Type relationshipType = relationshipAnnotation.relationshipType();

                EntityDescription<?> destinationEntity = getEntity((Class)type);

                if (destinationEntity == null) {
                    throw new RuntimeException("Unknown type " + type + " for relationship " + name + " in entity " + entity.getName());
                }

                Class<?> containerType = relationshipType == Type.TO_MANY ? Set.class : type;
                relationship = new RelationshipDescription(entity, name, containerType, relationshipType, destinationEntity, null);
                relationshipAnnotations.put(relationship, relationshipAnnotation);

                entity.addProperty(relationship);
            }
        }

        for (EntityDescription<?> entity : m_entities.values()) {
            for (RelationshipDescription relationship : entity.getRelationships()) {
                EntityDescription<?> destinationEntity = relationship.getDestinationEntity();
                // TODO: pluralize inverseName for to-many relationship by default
                String inverseName = entity.getName().toLowerCase(Locale.ENGLISH);
                Relationships.Relationship relationshipAnnotation = relationshipAnnotations.get(relationship);
                if (relationshipAnnotation != null) {
                    inverseName = relationshipAnnotation.inverseName();
                }
                try {
                    RelationshipDescription inverseRelationship = (RelationshipDescription) destinationEntity.getProperty(inverseName);
                    Class<?> inverseRelationshipType = relationshipAnnotations.get(inverseRelationship).type();
                    if (!inverseRelationshipType.isAssignableFrom(relationship.getEntity().getType())) {
                        throw new RuntimeException("Incompatible relationship type between "+entity.getName()+"#"+relationship.getName()+" and "+inverseRelationship.getEntity().getName()+"#"+inverseName);
                    }
                    relationship.setInverse(inverseRelationship);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
