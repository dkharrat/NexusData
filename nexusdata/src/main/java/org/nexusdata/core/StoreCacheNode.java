package org.nexusdata.core;

import java.util.HashMap;
import java.util.Map;


public class StoreCacheNode {
    private final ObjectID m_id;
    private Map<String,Object> m_properties;

    public StoreCacheNode(ObjectID id) {
        m_id = id;
        setProperties(null);
    }

    public ObjectID getID() {
        return m_id;
    }

    public void setProperty(String name, Object value) {
        m_properties.put(name, value);
    }

    public void setProperties(Map<String,Object> properties) {
        if (properties == null) {
            m_properties = new HashMap<String,Object>();
        } else {
            m_properties = properties;
        }
    }

    public Object getProperty(String name) {
        return m_properties.get(name);
    }

    public boolean hasProperty(String name) {
        return m_properties.containsKey(name);
    }

    public Map<String,Object> getProperties() {
        return m_properties;
    }
}
