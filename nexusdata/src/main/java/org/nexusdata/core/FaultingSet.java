package org.nexusdata.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.nexusdata.metamodel.RelationshipDescription;

class FaultingSet<E extends ManagedObject> implements Set<E> {

    private final ManagedObject parent;
    private Set<E> backingSet = new LinkedHashSet<E>();
    private boolean isFault = false;
    private final RelationshipDescription relationship;

    FaultingSet(ManagedObject parent, RelationshipDescription relationship, Collection<E> objects) {
        this.parent = parent;
        this.relationship = relationship;
        isFault = !this.parent.isInserted() && objects == null;
        if (objects != null) {
            setObjects(objects);
        }
    }

    private ObjectContext getContext() {
        return parent.getObjectContext();
    }

    void fulfillFaultIfNecessary() {
        if (isFault()) {
            getContext().faultInObjectRelationship(parent, relationship);
            isFault = false;
        }
    }

    public boolean isFault() {
        return isFault;
    }

    public void refresh() {
        isFault = !parent.isInserted();
        backingSet = new LinkedHashSet<E>();
    }

    void setObjects(Collection<E> objects) {
        backingSet = new LinkedHashSet<E>(objects);
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

        FaultingSet<E> relatedObjectsSet = new FaultingSet<E>(otherContext.objectWithID(parent.getID()), relationship, relatedObjects);
        return relatedObjectsSet;
    }

    private void setInverseRelationshipValue(ManagedObject relatedObject) {
        relatedObject.setValueDirectly(relationship.getInverse(), parent);
        parent.notifyManagedObjectContextOfChange();
    }

    private void clearInverseRelationshipValue(ManagedObject relatedObject) {
        relatedObject.setValueDirectly(relationship.getInverse(), null);
        parent.notifyManagedObjectContextOfChange();
    }

    @Override
    public boolean add(E object) {
        if (object.getObjectContext() != null && object.getObjectContext() != getContext()) {
            throw new UnsupportedOperationException("Cannot add an object that belongs to another context");
        }

        if (!object.isInserted() && object.getObjectContext() == null) {
            throw new UnsupportedOperationException("Cannot add an object that is not registered with a context");
        }

        fulfillFaultIfNecessary();

        setInverseRelationshipValue(object);

        if (object.isInserted()) {
            getContext().insert(object);
        }
        object.notifyManagedObjectContextOfChange();

        return backingSet.add(object);
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

        for (E object : backingSet) {
            clearInverseRelationshipValue(object);
        }

        backingSet.clear();
    }

    @Override
    public boolean contains(Object object) {
        fulfillFaultIfNecessary();
        return backingSet.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        fulfillFaultIfNecessary();
        return backingSet.containsAll(objects);
    }

    @Override
    public boolean isEmpty() {
        fulfillFaultIfNecessary();
        return backingSet.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        fulfillFaultIfNecessary();
        return backingSet.iterator();
    }

    @Override
    public boolean remove(Object o) {
        fulfillFaultIfNecessary();

        if (o instanceof ManagedObject) {
            ManagedObject object = (ManagedObject)o;
            clearInverseRelationshipValue(object);
            if (object.isInserted()) {
                getContext().delete(object);
            } else {
                object.notifyManagedObjectContextOfChange();
            }
        }

        return backingSet.remove(o);
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
        return backingSet.size();

    }

    @Override
    public Object[] toArray() {
        fulfillFaultIfNecessary();
        return backingSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        fulfillFaultIfNecessary();
        return backingSet.toArray(array);
    }

    @Override
    public String toString() {
        if (isFault()) {
            return "Relationship '"+ relationship.getName()+"' fault on object "+ parent.toObjectReferenceString();
        } else {
            return "Relationship '"+ relationship.getName()+"' on object "+ parent.toObjectReferenceString()+";\n"+
                    "   values: "+ backingSet.toString();
        }
    }

    public Set<ObjectID> getObjectIDs() {
        fulfillFaultIfNecessary();

        Set<ObjectID> objectIDs = new LinkedHashSet<ObjectID>();
        for (ManagedObject object : backingSet) {
            objectIDs.add(object.getID());
        }
        return objectIDs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((backingSet == null)   ? 0 : backingSet.hashCode());
        result = prime * result + ((parent == null)       ? 0 : parent.hashCode());
        result = prime * result + ((relationship == null) ? 0 : relationship.hashCode());
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
        if (backingSet == null) {
            if (other.backingSet != null)
                return false;
        } else if (!backingSet.equals(other.backingSet))
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        if (relationship == null) {
            if (other.relationship != null)
                return false;
        } else if (!relationship.equals(other.relationship))
            return false;
        return true;
    }
}
