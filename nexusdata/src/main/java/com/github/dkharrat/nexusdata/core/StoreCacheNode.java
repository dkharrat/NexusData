package com.github.dkharrat.nexusdata.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data cache for a single record in the persistent store. It's used by the framework to retrieve and
 * store the data of the corresponding managed object from and to the persistent store.
 *
 * Internally, a StoreCacheNode is simply a key/value store to get or set value of individual properties.
 * A value can contain references to other ObjectIDs if the property is a relationship.
 */
public class StoreCacheNode {
    private final ObjectID id;
    private Map<String,Object> properties = new HashMap<String,Object>();

    /**
     * Creates a new StoreCacheNode that's associated with the specified ObjectID
     *
     * @param id the objectID that this node will represent
     */
    public StoreCacheNode(ObjectID id) {
        this.id = id;
    }

    /**
     * Returns the ObjectID represented by this StoreCacheNode.
     *
     * @return the ObjectID represented by this StoreCacheNode
     */
    public ObjectID getID() {
        return id;
    }

    /**
     * Sets the value for the given property. For attributes, the value must be of type that is supported, like {@link String},
     * {@link Integer}, {@link java.util.Date}, {@link Enum}, etc. For a to-one relationship, the value must be the
     * {@link ObjectID} of the related object. For a to-many relationship, the value must be the a {@code Set<ObjectID>}
     * containing the ObjectIDs of the related objects.
     *
     * @param name  the property name to set
     * @param value the value of the property
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Sets this node to have the properties specified from the input map. Previously set properties in this node will
     * be removed.
     *
     * @param props  a Map containing the key/value pairs for the properties to be set. For keys representing attributes,
     *               their value must be of type that is supported, like {@link String}, {@link Integer},
     *               {@link java.util.Date}, {@link Enum}, etc. For keys representing a to-one relationship, the value
     *               must be the {@link ObjectID} of the related object. For a to-many relationship, the value must be
     *               the a {@code Set<ObjectID>} containing the ObjectIDs of the related objects.
     */
    public void setProperties(Map<String,Object> props) {
        properties.clear();
        properties.putAll(props);
    }

    /**
     * Returns the value of the given property. For attributes, the value will be of type that is one of the supported types like {@link String},
     * {@link Integer}, {@link java.util.Date}, {@link Enum}, etc. For a to-one relationship, the value must be the
     * {@link ObjectID} of the related object. For a to-many relationship, the value must be the a {@code Set<ObjectID>}
     * containing the ObjectIDs of the related objects.
     *
     * @param name  the property name to get
     *
     * @return the value of the specified property
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Returns true if this node is storing a value for the specified property, or false otherwise.
     *
     * @param name  the property name to check
     *
     * @return true if this node is storing a value for the specified property, or false otherwise
     */
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * Returns the key/value store for all the properties in this node.
     *
     * @return the key/value store for all the properties in this node
     */
    public Map<String,Object> getProperties() {
        return properties;
    }
}
