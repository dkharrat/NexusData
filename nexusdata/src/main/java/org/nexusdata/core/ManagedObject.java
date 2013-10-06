package org.nexusdata.core;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.nexusdata.metamodel.*;
import org.nexusdata.utils.ObjectUtil;
import org.nexusdata.metamodel.PropertyDescription;
import org.nexusdata.utils.ObjectUtil;


/* TODO: ManagedObject changes
   - handle case when object is unregistered from context (e.g. return null values on gets)
*/

public class ManagedObject {

    private ObjectID m_id;
    ObjectContext m_managedObjectContext;
    private boolean m_isFault = false;
    private final Map<String,Object> m_values = new HashMap<String,Object>();

    private final PropertyChangeSupport m_propertyChangeSupport = new PropertyChangeSupport(this);

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
            ((ManagedObject)object).m_isFault = true;
        }

        return object;
    }

    protected void init() {
        for (AttributeDescription attr : getEntity().getAttributes()) {
            if (attr.getDefaultValue() != null) {
                m_values.put(attr.getName(), attr.getDefaultValue());
            }
        }
    }

    public <T extends ManagedObject> ObjectID getID() {
        return m_id;
    }

    void setID(ObjectID id) {
        m_id = id;
    }

    public EntityDescription<?> getEntity() {
        return m_id.getEntity();
    }

    public ObjectContext getObjectContext() {
        return m_managedObjectContext;
    }

    void setManagedObjectContext(ObjectContext context) {
        m_managedObjectContext = context;
    }

    protected void notifyManagedObjectContextOfChange() {
        if (m_managedObjectContext != null) {
            m_managedObjectContext.markObjectAsUpdated(this);
        }
    }

    void fulfillFaultIfNecessary() {
        if (m_isFault) {
            getObjectContext().faultInObject(this);
            m_isFault = false;
        }
    }

    public Object getValue(String propertyName) {
        fulfillFaultIfNecessary();

        PropertyDescription property;
        try {
            property = getEntity().getProperty(propertyName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Invalid property name: " + propertyName, e);
        }

        Object value = getValueDirectly(property);

        if (value == null && property.isRelationship()) {
            RelationshipDescription relationship = (RelationshipDescription)property;
            if (relationship.isToMany()) {
                value = new FaultingSet<ManagedObject>(this, relationship, null);
                setValueDirectly(property, value);
            }
        }

        return value;
    }

    Object getValueDirectly(PropertyDescription property) {
        return m_values.get(property.getName());
    }

    void setValueDirectly(PropertyDescription property, Object value) {
        String propertyName = property.getName();
        if (value != null && !property.getType().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Invalid value "+value+" for property: " + propertyName + " of entity " + getEntity().getName());
        }
        m_values.put(propertyName, value);
    }

    private void setValue(PropertyDescription property, Object newValue, Object oldValue) {
        setValueDirectly(property, newValue);

        if (property.isRelationship()) {
            RelationshipDescription relationship = (RelationshipDescription)property;
            RelationshipDescription inverseRelationship = relationship.getInverse();

            if (relationship.isToMany()) {
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
                        ManagedObject oldRelatedObject = (ManagedObject)oldValue;
                        FaultingSet<ManagedObject> relatedObjects = (FaultingSet<ManagedObject>) oldRelatedObject.getValue(inverseRelationship.getName());
                        relatedObjects.remove(this);
                    }

                    if (newValue != null) {
                        ManagedObject relatedObject = (ManagedObject)newValue;
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
        m_propertyChangeSupport.firePropertyChange(property.getName(), oldValue, newValue);
    }

    public boolean setValue(String propertyName, Object value) {
        try {
            PropertyDescription property = getEntity().getProperty(propertyName);
            boolean changed = false;

            Object oldValue = getValue(propertyName);       // this should trigger a fault if necessary
            if (!ObjectUtil.objectsEqual(oldValue, value)) {
                changed = true;

                if (property.isRelationship()) {
                    RelationshipDescription relationship = (RelationshipDescription)property;
                    if (relationship.isToMany()) {
                        FaultingSet<?> oldRelatedObjects = (FaultingSet<?>)oldValue;
                        if (value == null && !oldRelatedObjects.isEmpty()) {
                            value = oldValue;   // do not null out collection; empty it instead
                        } else {
                            if (value != null && !(value instanceof Collection)) {
                                throw new IllegalArgumentException("Expected a Collection for property: " + propertyName + ", but got " + value);
                            }
                            if (!(value instanceof FaultingSet)) {
                                value = new FaultingSet(this, relationship, (Collection<?>)value);
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
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Invalid property name: " + propertyName, e);
        }
    }

    public Map<String,Object> getAttributeValues() {
        Map<String,Object> values = new HashMap<String,Object>();

        for (PropertyDescription property : getEntity().getAttributes()) {
            values.put(property.getName(), getValue(property.getName()));
        }

        return values;
    }

    public Map<String,Object> getValues() {
        Map<String,Object> values = new HashMap<String,Object>();

        for (PropertyDescription property : getEntity().getProperties()) {
            values.put(property.getName(), getValue(property.getName()));
        }

        return values;
    }

    void setValuesDirectly(Map<String,Object> values) {
        String lastPropertyName = "";   // used for exception description

        try {
            for (String propertyName : values.keySet()) {
                lastPropertyName = propertyName;
                PropertyDescription property = getEntity().getProperty(propertyName);

                Object value = values.get(propertyName);
                setValueDirectly(property, value);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Invalid property name: " + lastPropertyName, e);
        }
    }

    public void setValues(Map<String,Object> values) {
        for (String propertyName : values.keySet()) {
            Object value = values.get(propertyName);
            //TODO: this will trigger multiple notifications; instead, consolidate the notifications to one
            setValue(propertyName, value);
        }
    }

    public void refresh() {
        if (!m_isFault) {
            m_isFault = true;

            for (RelationshipDescription relationship : getEntity().getRelationships()) {
                refreshRelationship(relationship.getName());
            }

            getObjectContext().markObjectAsRefreshed(this);
        }
    }

    public boolean isNew() {
        return m_id.isTemporary() || isInserted();
    }

    public boolean isInserted() {
        return getObjectContext() != null && getObjectContext().isInserted(this);
    }

    public boolean isUpdated() {
        return getObjectContext() != null && getObjectContext().isUpdated(this);
    }

    public boolean isDeleted() {
        return getObjectContext() != null && getObjectContext().isDeleted(this);
    }

    public boolean hasChanges() {
        return isInserted() || isDeleted() || isUpdated();
    }

    public boolean isFault() {
        return m_isFault && !isNew();
    }

    public void refreshRelationship(String relationshipName) {
        RelationshipDescription relationship;
        try {
            relationship = getEntity().getRelationship(relationshipName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Invalid relationship name: " + relationshipName, e);
        }
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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        m_propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        m_propertyChangeSupport.removePropertyChangeListener(listener);
    }

    String toObjectReferenceString() {
        return new StringBuilder(20).append("<").
                append(getEntity().getName()).append(": 0x").
                append(Integer.toHexString(System.identityHashCode(this))).
                append(">").toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(toObjectReferenceString()).append(" (")
            .append("id: ").append(getID()).append("; data: ");

        if (isFault()) {
            return sb.append("<fault>)").toString();
        } else {
            sb.append("{\n");

            for (PropertyDescription property : getEntity().getProperties()) {
                sb.append("   ").append(property.getName()).append(" = ");

                final Object propertyValue = getValue(property.getName());
                if (property.isRelationship()) {
                    RelationshipDescription relationship = ((RelationshipDescription)property);
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
