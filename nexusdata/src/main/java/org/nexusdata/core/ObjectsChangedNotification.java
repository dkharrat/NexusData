package org.nexusdata.core;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents the event that is sent when a set of objects in an {@link ObjectContext} have changed. This event is sent
 * out from {@link ObjectContextNotifier}.
 *
 * @see ObjectContextNotifier.ObjectContextListener#notifyListenersOfObjectsChanged(ObjectContext, ObjectsChangedNotification)
 */
public class ObjectsChangedNotification extends ChangedObjectsSet {

    private final Set<ManagedObject> refreshedObjects;

    ObjectsChangedNotification() {
        this(new HashSet<ManagedObject>(), new HashSet<ManagedObject>(), new HashSet<ManagedObject>(), new HashSet<ManagedObject>());
    }

    ObjectsChangedNotification(Set<ManagedObject> insertedObjects,
                               Set<ManagedObject> updatedObjects,
                               Set<ManagedObject> deletedObjects,
                               Set<ManagedObject> refreshedObjects) {
        super(insertedObjects, updatedObjects, deletedObjects);
        this.refreshedObjects = refreshedObjects;
    }

    ObjectsChangedNotification(ChangedObjectsSet changedObjects,
                               Set<ManagedObject> refreshedObjects) {
        this(changedObjects.getInsertedObjects(), changedObjects.getUpdatedObjects(), changedObjects.getDeletedObjects(), refreshedObjects);
    }

    @Override
    void clear() {
        super.clear();
        refreshedObjects.clear();
    }

    /**
     * Returns the objects that have been refreshed in the context (see {@link ManagedObject#refresh()} for details on
     * refreshed objects.
     *
     * @return the set of objects that have been refreshed
     */
    public Set<ManagedObject> getRefreshedObjects() {
        return refreshedObjects;
    }

    void objectRefreshed(ManagedObject object) {
        refreshedObjects.add(object);
    }

    /**
     * Indicates whether the specified object has been refreshed.
     *
     * @param object    the object to check whether it was refreshed
     *
     * @return  true if the specified object has been refreshed, or false otherwise
     */
    public boolean isRefreshed(ManagedObject object) {
        return refreshedObjects.contains(object);
    }
}
