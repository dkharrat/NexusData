package org.nexusdata.core;

import java.util.HashMap;
import java.util.Map;


public class StoreCacheNode {
    private final ObjectID id;
    private Map<String,Object> properties;

    public StoreCacheNode(ObjectID id) {
        this.id = id;
        setProperties(null);
    }

    public ObjectID getID() {
        return id;
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public void setProperties(Map<String,Object> properties) {
        if (properties == null) {
            this.properties = new HashMap<String,Object>();
        } else {
            this.properties = properties;
        }
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public Map<String,Object> getProperties() {
        return properties;
    }
}
