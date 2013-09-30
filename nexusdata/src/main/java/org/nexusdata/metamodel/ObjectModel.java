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
    private final String m_name;
    private final Map<String,EntityDescription<?>> m_entities = new HashMap<String,EntityDescription<?>>();

    public ObjectModel(String name, List<EntityDescription<?>> entities, int version) {
        m_name = name;
        m_version = version;
        initEntities(entities);
    }

    public ObjectModel(InputStream modelData) throws IOException {
        ObjectModelJsonParser.ParsedModel parsedModel = ObjectModelJsonParser.parseJsonModel(this, modelData);
        m_name = parsedModel.getName();
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
}
