package org.nexusdata.core;

import java.util.HashSet;
import java.util.Set;


public class ChangedObjectsSet {

    private final Set<ManagedObject> m_insertedObjects;
    private final Set<ManagedObject> m_updatedObjects;
    private final Set<ManagedObject> m_deletedObjects;

    ChangedObjectsSet() {
        this(new HashSet<ManagedObject>(), new HashSet<ManagedObject>(), new HashSet<ManagedObject>());
    }

    ChangedObjectsSet(ChangedObjectsSet other) {
        this(new HashSet<ManagedObject>(other.getInsertedObjects()),
                new HashSet<ManagedObject>(other.getUpdatedObjects()),
                new HashSet<ManagedObject>(other.getDeletedObjects()));
    }

    ChangedObjectsSet(Set<ManagedObject> insertedObjects, Set<ManagedObject> updatedObjects, Set<ManagedObject> deletedObjects) {
        m_insertedObjects = insertedObjects;
        m_updatedObjects = updatedObjects;
        m_deletedObjects = deletedObjects;
    }

    void objectInserted(ManagedObject object) {
        if (!object.isDeleted()) {
            synchronized(m_insertedObjects) {
                m_insertedObjects.add(object);
            }
        }
        m_deletedObjects.remove(object);
    }

    void objectUpdated(ManagedObject object) {
        if (!m_insertedObjects.contains(object) && !m_deletedObjects.contains(object)) {
            m_updatedObjects.add(object);
        }
    }

    void objectDeleted(ManagedObject object, boolean trackDeletionEvenIfNew) {
        synchronized(m_insertedObjects) {
            m_insertedObjects.remove(object);
        }
        m_updatedObjects.remove(object);
        if (!object.isNew() || trackDeletionEvenIfNew) {
            m_deletedObjects.add(object);
        }
    }

    void clear() {
        synchronized(m_insertedObjects) {
            m_insertedObjects.clear();
        }
        m_deletedObjects.clear();
        m_updatedObjects.clear();
    }

    public Set<ManagedObject> getInsertedObjects() {
        return m_insertedObjects;
    }

    public Set<ManagedObject> getUpdatedObjects() {
        return m_updatedObjects;
    }

    public Set<ManagedObject> getDeletedObjects() {
        return m_deletedObjects;
    }

    public boolean isInserted(ManagedObject object) {
        return m_insertedObjects.contains(object);
    }

    public boolean isUpdated(ManagedObject object) {
        return m_updatedObjects.contains(object);
    }

    public boolean isDeleted(ManagedObject object) {
        return m_deletedObjects.contains(object);
    }

    public boolean hasChanges() {
        return  !m_insertedObjects.isEmpty() ||
                !m_updatedObjects.isEmpty() ||
                !m_deletedObjects.isEmpty();
    }
}
