package org.nexusdata.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.nexusdata.metamodel.RelationshipDescription;
import org.nexusdata.metamodel.RelationshipDescription;

class FaultingSet<E extends ManagedObject> implements Set<E> {

    private final ManagedObject m_parent;
    private Set<E> m_backingSet = new LinkedHashSet<E>();
    private boolean m_isFault = false;
    private final RelationshipDescription m_relationship;

    FaultingSet(ManagedObject parent, RelationshipDescription relationship, Collection<E> objects) {
        m_parent = parent;
        m_relationship = relationship;
        m_isFault = !m_parent.isNew() && objects == null;
        if (objects != null) {
            setObjects(objects);
        }
    }

    private ObjectContext getContext() {
        return m_parent.getObjectContext();
    }

    void fulfillFaultIfNecessary() {
        if (isFault()) {
            getContext().faultInObjectRelationship(m_parent, m_relationship);
            m_isFault = false;
        }
    }

    public boolean isFault() {
        return m_isFault;
    }

    public void refresh() {
        m_isFault = !m_parent.isNew();
        m_backingSet = new LinkedHashSet<E>();
    }

    void setObjects(Collection<E> objects) {
        m_backingSet = new LinkedHashSet<E>(objects);
    }

    /**
     * Clones this set such that it's compatible with a different context
     * @param otherContext
     */
    FaultingSet<E> getObjectsInContext(ObjectContext otherContext) {
        if (!getContext().isCompatibleWithContext(otherContext)) {
            throw new RuntimeException("Contexts not compatible. Ensure that they share the same persistence store.");
        }

        List<E> relatedObjects = null;
        if (!isFault()) {
            relatedObjects = new ArrayList<E>();
            for (E relatedObject : this) {
                relatedObjects.add((E) otherContext.objectWithID(relatedObject.getID()));
            }
        }

        FaultingSet<E> relatedObjectsSet = new FaultingSet<E>(otherContext.objectWithID(m_parent.getID()), m_relationship, relatedObjects);
        return relatedObjectsSet;
    }

    private void setInverseRelationshipValue(ManagedObject relatedObject) {
        relatedObject.setValueDirectly(m_relationship.getInverse(), m_parent);
        m_parent.notifyManagedObjectContextOfChange();
    }

    private void clearInverseRelationshipValue(ManagedObject relatedObject) {
        relatedObject.setValueDirectly(m_relationship.getInverse(), null);
        m_parent.notifyManagedObjectContextOfChange();
    }

    @Override
    public boolean add(E object) {
        if (object.getObjectContext() != null && object.getObjectContext() != getContext()) {
            throw new UnsupportedOperationException("Cannot add an object that belongs to another context");
        }

        if (!object.isNew() && object.getObjectContext() == null) {
            throw new UnsupportedOperationException("Cannot add an object that is not registered with a context");
        }

        fulfillFaultIfNecessary();

        setInverseRelationshipValue(object);

        if (object.isNew()) {
            getContext().insert(object);
        }
        object.notifyManagedObjectContextOfChange();

        return m_backingSet.add(object);
    }

    @Override
    public boolean addAll(Collection<? extends E> objects) {
        boolean changed = false;
        for (E object : objects) {
            changed = add(object) || changed;
        }
        return changed;
    }

    @Override
    public void clear() {
        fulfillFaultIfNecessary();

        for (E object : m_backingSet) {
            clearInverseRelationshipValue(object);
        }

        m_backingSet.clear();
    }

    @Override
    public boolean contains(Object object) {
        fulfillFaultIfNecessary();
        return m_backingSet.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        fulfillFaultIfNecessary();
        return m_backingSet.containsAll(objects);
    }

    @Override
    public boolean isEmpty() {
        fulfillFaultIfNecessary();
        return m_backingSet.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        fulfillFaultIfNecessary();
        return m_backingSet.iterator();
    }

    @Override
    public boolean remove(Object o) {
        fulfillFaultIfNecessary();

        if (o instanceof ManagedObject) {
            ManagedObject object = (ManagedObject)o;
            clearInverseRelationshipValue(object);
            if (object.isNew()) {
                getContext().delete(object);
            } else {
                object.notifyManagedObjectContextOfChange();
            }
        }

        return m_backingSet.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        boolean changed = false;
        for (Object object : objects) {
            changed = remove(object) || changed;
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int size() {
        fulfillFaultIfNecessary();
        return m_backingSet.size();

    }

    @Override
    public Object[] toArray() {
        fulfillFaultIfNecessary();
        return m_backingSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        fulfillFaultIfNecessary();
        return m_backingSet.toArray(array);
    }

    @Override
    public String toString() {
        if (isFault()) {
            return "Relationship '"+m_relationship.getName()+"' fault on object "+m_parent.toObjectReferenceString();
        } else {
            return "Relationship '"+m_relationship.getName()+"' on object "+m_parent.toObjectReferenceString()+";\n"+
                    "   values: "+m_backingSet.toString();
        }
    }

    public Set<ObjectID> getObjectIDs() {
        fulfillFaultIfNecessary();

        Set<ObjectID> objectIDs = new LinkedHashSet<ObjectID>();
        for (ManagedObject object : m_backingSet) {
            objectIDs.add(object.getID());
        }
        return objectIDs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_backingSet == null)   ? 0 : m_backingSet.hashCode());
        result = prime * result + ((m_parent == null)       ? 0 : m_parent.hashCode());
        result = prime * result + ((m_relationship == null) ? 0 : m_relationship.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FaultingSet<?> other = (FaultingSet<?>) obj;
        if (m_backingSet == null) {
            if (other.m_backingSet != null)
                return false;
        } else if (!m_backingSet.equals(other.m_backingSet))
            return false;
        if (m_parent == null) {
            if (other.m_parent != null)
                return false;
        } else if (!m_parent.equals(other.m_parent))
            return false;
        if (m_relationship == null) {
            if (other.m_relationship != null)
                return false;
        } else if (!m_relationship.equals(other.m_relationship))
            return false;
        return true;
    }
}
