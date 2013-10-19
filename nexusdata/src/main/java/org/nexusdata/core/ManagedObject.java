package org.nexusdata.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.nexusdata.metamodel.*;
import org.nexusdata.utils.ObjectUtil;
import org.nexusdata.metamodel.Property;


// TODO: handle case when object is unregistered from context (e.g. return null values on gets)

/**
 * A managed object is the base type of any object instance in Nexus Data. It stores the object's properties defined by
 * its associated {@link org.nexusdata.metamodel.Entity}. Each object is uniquely identified by a global ID represented by {@link
 * ObjectID}.
 * <p>
 * An object is managed by an {@link ObjectContext} and can be persisted in a persistence store. Typically, it is
 * sub-classed into more specialized types to define more concrete behavior. An object must not be created directly.
 * Instead, an object context is used to manage an object's lifecycle.
 * <p>
 * In some situations, an object may be a "fault", meaning that its associated data has not been fully retrieved from
 * its persistence store. The advantage of this is that memory is not allocated for storage of the data until its
 * actually needed. When any of the object's properties is accessed, a fault is fired to retrieve the data from the
 * persistence store. This entire process is fully transparent to an application and it does not directly need to know
 * about it.
 */
public class ManagedObject {

    private ObjectID id;
    ObjectContext managedObjectContext;
    private boolean isFault = false;
    private final Map<String,Object> values = new HashMap<String,Object>();

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * The constructor of the managed object. Subclasses must provide a public default constructor with no parameters,
     * so that Nexus Data can create an instance of the class when requested.
     */
    protected ManagedObject() {
    }

    @SuppressWarnings("unchecked")
    static <T extends ManagedObject> T newObject(ObjectID id) {
        T object;

        try {
            object = (T) id.getType().newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Class " + id.getType() + " must have a default public constructor with no parameters", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Class " + id.getType() + " must have a default public constructor with no parameters", e);
        }

        object.setID(id);
        if (!id.isTemporary()) {
            ((ManagedObject)object).isFault = true;
        }

        return object;
    }

    /**
     * Initializes the object after it's created. The object context calls this method whenever an instance is
     * instantiated to initialize the state of the object. Subclasses can override this method to provide further
     * initialization if needed. Subclasses must call the super class's implementation of this method when it's
     * overridden.
     */
    protected void init() {
        for (Attribute attr : getEntity().getAttributes()) {
            if (attr.getDefaultValue() != null) {
                values.put(attr.getName(), attr.getDefaultValue());
            }
        }
    }

    /**
     * Returns the object's global ID. When a new object is created. It is assigned a temporary ID, which is later
     * converted into a permanent one when saved to a persistence store.
     *
     * @return the object's global ID
     */
    public <T extends ManagedObject> ObjectID getID() {
        return id;
    }

    void setID(ObjectID id) {
        this.id = id;
    }

    /**
     * Returns the object's associated entity.
     *
     * @return the object's associated entity
     */
    public Entity<?> getEntity() {
        return id.getEntity();
    }

    /**
     * Returns the object's associated context.
     *
     * @return the object's associated context
     */
    public ObjectContext getObjectContext() {
        return managedObjectContext;
    }

    void setManagedObjectContext(ObjectContext context) {
        managedObjectContext = context;
    }

    void notifyManagedObjectContextOfChange() {
        if (managedObjectContext != null) {
            managedObjectContext.markObjectAsUpdated(this);
        }
    }

    void fulfillFaultIfNecessary() {
        if (isFault) {
            getObjectContext().faultInObject(this);
            isFault = false;
        }
    }

    /**
     * Returns a property's value for this object.
     *
     * @param propertyName  the name of the property to retrieve its value
     * @return the value of the specified property
     */
    public Object getValue(String propertyName) {
        fulfillFaultIfNecessary();

        Property property = getEntity().getProperty(propertyName);

        Object value = getValueDirectly(property);

        if (value == null && property.isRelationship()) {
            Relationship relationship = (Relationship)property;
            if (relationship.isToMany()) {
                value = new FaultingSet<ManagedObject>(this, relationship, null);
                setValueDirectly(property, value);
            }
        }

        return value;
    }

    Object getValueDirectly(Property property) {
        return values.get(property.getName());
    }

    void setValueDirectly(Property property, Object value) {
        String propertyName = property.getName();
        if (value != null && !property.getType().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Invalid value "+value+" for property: " + propertyName + " of entity " + getEntity().getName());
        }
        values.put(propertyName, value);
    }

    private void setValue(Property property, Object newValue, Object oldValue) {
        setValueDirectly(property, newValue);

        if (property.isRelationship()) {
            Relationship relationship = (Relationship)property;
            Relationship inverseRelationship = relationship.getInverse();

            if (relationship.isToMany()) {
                @SuppressWarnings("unchecked")
                FaultingSet<ManagedObject> relatedObjects = ((FaultingSet<ManagedObject>)newValue);
                for (ManagedObject relatedObject : relatedObjects) {
                    if (inverseRelationship.isToOne()) {
                        relatedObject.setValue(inverseRelationship.getName(), this);
                    } else {
                        throw new UnsupportedOperationException("many-to-many relationships are not supported yet");
                    }
                }
            } else {    // to-one relationship
                if (inverseRelationship.isToMany()) {
                    if (oldValue != null) {
                        @SuppressWarnings("unchecked")
                        ManagedObject oldRelatedObject = (ManagedObject)oldValue;
                        @SuppressWarnings("unchecked")
                        FaultingSet<ManagedObject> relatedObjects = (FaultingSet<ManagedObject>) oldRelatedObject.getValue(inverseRelationship.getName());
                        relatedObjects.remove(this);
                    }

                    if (newValue != null) {
                        @SuppressWarnings("unchecked")
                        ManagedObject relatedObject = (ManagedObject)newValue;
                        @SuppressWarnings("unchecked")
                        FaultingSet<ManagedObject> relatedObjects = (FaultingSet<ManagedObject>) relatedObject.getValue(inverseRelationship.getName());
                        relatedObjects.add(this);
                    }
                } else {
                    if (oldValue != null) {
                        ManagedObject oldRelatedObject = (ManagedObject)oldValue;
                        oldRelatedObject.setValue(inverseRelationship.getName(), null);
                    }

                    if (newValue != null) {
                        ManagedObject relatedObject = (ManagedObject)newValue;
                        relatedObject.setValue(inverseRelationship.getName(), this);
                    }
                }
            }
        }

        notifyManagedObjectContextOfChange();
        propertyChangeSupport.firePropertyChange(property.getName(), oldValue, newValue);
    }

    /**
     * Sets the value for a property.
     *
     * @param propertyName  the name of the property to set
     * @param value         the value of the property to set
     * @throws IllegalArgumentException if an invalid value is set for the property
     * @return true if the value was changed, or false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean setValue(String propertyName, Object value) {
        Property property = getEntity().getProperty(propertyName);
        boolean changed = false;

        Object oldValue = getValue(propertyName);       // this should trigger a fault if necessary
        if (!ObjectUtil.objectsEqual(oldValue, value)) {
            changed = true;

            if (property.isRelationship()) {
                Relationship relationship = (Relationship)property;
                if (relationship.isToMany()) {
                    FaultingSet<?> oldRelatedObjects = (FaultingSet<?>)oldValue;
                    if (value == null && !oldRelatedObjects.isEmpty()) {
                        value = oldValue;   // do not null out collection; empty it instead
                    } else {
                        if (value != null && !(value instanceof Collection)) {
                            throw new IllegalArgumentException("Expected a Collection for property: " + propertyName + ", but got " + value);
                        }
                        if (!(value instanceof FaultingSet)) {
                            value = new FaultingSet<ManagedObject>(this, relationship, (Collection<ManagedObject>)value);
                        }

                        changed = !oldRelatedObjects.equals(value);
                    }

                    if (changed) {
                        oldRelatedObjects.clear();
                    }
                }
            }

            if (changed) {
                setValue(property, value, oldValue);
            }
        }
        return changed;
    }

    /**
     * Returns a map of the values of all the attributes of this object.
     *
     * @return a map holding the key/value pairs for all the attribute values of this object. The key represents the
     *         name of the attribute, and the value represents the value of the attribute.
     */
    public Map<String,Object> getAttributeValues() {
        Map<String,Object> values = new HashMap<String,Object>();

        for (Property property : getEntity().getAttributes()) {
            values.put(property.getName(), getValue(property.getName()));
        }

        return values;
    }

    /**
     * Returns a map of the values of all the properties (attributes and relationships) of this object.
     *
     * @return a map holding the key/value pairs for all the property values of this object. The key represents the
     *         name of the property, and the value represents the value of the property.
     */
    public Map<String,Object> getValues() {
        Map<String,Object> values = new HashMap<String,Object>();

        for (Property property : getEntity().getProperties()) {
            values.put(property.getName(), getValue(property.getName()));
        }

        return values;
    }

    void setValuesDirectly(Map<String,Object> values) {
        for (String propertyName : values.keySet()) {
            Property property = getEntity().getProperty(propertyName);

            Object value = values.get(propertyName);
            setValueDirectly(property, value);
        }
    }

    /**
     * Sets the value for each property specified in the map.
     *
     * @param values    a map holding a value for each property name to be set
     */
    public void setValues(Map<String,Object> values) {
        for (String propertyName : values.keySet()) {
            Object value = values.get(propertyName);
            //TODO: this will trigger multiple notifications; instead, consolidate the notifications to one
            setValue(propertyName, value);
        }
    }

    /**
     * Discards any changes made to this object. This is done by marking the object as a fault. Its data will be
     * retrieved from its persistence store then next time any property is accessed.
     */
    public void refresh() {
        if (!isFault) {
            isFault = true;
            values.clear();

            for (Relationship relationship : getEntity().getRelationships()) {
                refreshRelationship(relationship.getName());
            }

            getObjectContext().markObjectAsRefreshed(this);
        }
    }

    /**
     * Returns true if this object has been inserted into an object context and is pending insertion to its
     * persistence store.
     *
     * @return  true if the object has been inserted into this context, or false otherwise
     */
    public boolean isInserted() {
        return getObjectContext() != null && getObjectContext().isInserted(this);
    }

    /**
     * Returns true if this object has been updated in that any of its properties have changed since it was last
     * saved.
     *
     * @return  true if the object has been updated, or false otherwise
     */
    public boolean isUpdated() {
        return getObjectContext() != null && getObjectContext().isUpdated(this);
    }

    /**
     * Returns true if this object is pending deletion from its persistence store during the next save.
     *
     * @return  true if the object is pending deletion, or false otherwise
     */
    public boolean isDeleted() {
        return getObjectContext() != null && getObjectContext().isDeleted(this);
    }

    /**
     * Returns true if this object has been inserted, changed, or deleted.
     *
     * @see #isInserted()
     * @see #isUpdated()
     * @see #isDeleted()
     * @return true if this object has been inserted, changed, or deleted.
     */
    public boolean hasChanges() {
        return isInserted() || isDeleted() || isUpdated();
    }

    /**
     * Returns true if the object is in a fault state. See the class description for details about faulting.
     *
     * @return true if the object is a fault, or false otherwise
     */
    public boolean isFault() {
        return isFault && !isInserted();
    }

    /**
     * Discards any changes made to the specified relationship.
     *
     * @param relationshipName the name of the relationship to refresh
     */
    public void refreshRelationship(String relationshipName) {
        Relationship relationship = getEntity().getRelationship(relationshipName);

        Object value = getValueDirectly(relationship);
        if (value != null) {
            if (relationship.isToMany()) {
                FaultingSet<?> relatedObjects = (FaultingSet<?>) value;
                relatedObjects.refresh();
            } else {
                ManagedObject relatedObject = (ManagedObject) value;
                relatedObject.refresh();
            }
        }
    }

    /**
     * Adds a property listener to this object. When a change is made to any property, a notification is fired to all
     * listeners.
     *
     * @param listener the listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property listener from this object so that it will no longer receive property change notifications.
     *
     * @param listener  the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    String toObjectReferenceString() {
        return new StringBuilder(20).append("<").
                append(getEntity().getName()).append(": 0x").
                append(Integer.toHexString(System.identityHashCode(this))).
                append(">").toString();
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * object.
     *
     * @return  a printable representation of this object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(toObjectReferenceString()).append(" (")
            .append("id: ").append(getID()).append("; data: ");

        if (isFault()) {
            return sb.append("<fault>)").toString();
        } else {
            sb.append("{\n");

            for (Property property : getEntity().getProperties()) {
                sb.append("   ").append(property.getName()).append(" = ");

                final Object propertyValue = getValue(property.getName());
                if (property.isRelationship()) {
                    Relationship relationship = ((Relationship)property);
                    if (relationship.isToMany()) {
                        FaultingSet<?> relatedObjects = ((FaultingSet<?>)propertyValue);
                        if (relatedObjects.isFault()) {
                            sb.append("<relationship fault>");
                        } else {
                            sb.append(relatedObjects.getObjectIDs());
                        }
                    } else {
                        ManagedObject relatedObject = (ManagedObject)propertyValue;
                        if (relatedObject == null) {
                            sb.append("<null>");
                        } else {
                            sb.append(relatedObject.getID());
                        }
                    }
                } else {
                    sb.append(propertyValue);
                }
                sb.append("\n");
            }

            return sb.append("})").toString();
        }
    }
}
