package org.nexusdata.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nexusdata.metamodel.Entity;
import org.nexusdata.metamodel.Relationship;

/**
 * A PersistentStore represents the data store where the object graph is persisted. A persistence store is uniquely
 * identified by a UUID, which is also persisted in the store along with other metadata. To create a persistence store,
 * use a concrete implementation. There are two kinds of PersistentStore:
 * <ul>
 *     <li>{@link AtomicStore}: A store in which the object graph is loaded and saved together all at once.</li>
 *     <li>{@link IncrementalStore}: A store in which the object graph is loaded and saved in chunks, as needed.</li>
 * </ul>
 */
public abstract class PersistentStore {

    private static final String UUID_KEY    = "_UUID";

    private PersistentStoreCoordinator storeCoordinator;
    private final URL location;
    private final Map<String,Object> metadata = new HashMap<String,Object>();

    /**
     * Constructs a new PersistentStore
     *
     * @param location  the location in which to save the data persistence file
     */
    PersistentStore(URL location) {
        this.location = location;
    }

    PersistentStore(File location) {
        URL url;
        try {
            url = location.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.location = url;
    }

    /**
     * Called when the PersistentStore is first loaded in order to load the store's metadata (e.g. store UUID, type, etc.).
     * A store's metadata must at minimum contain the store's UUID, which can be set by calling {@link #setUuid(UUID)}.
     */
    protected abstract void loadMetadata();

    /**
     * Returns the persistent store coordinator that owns this persistent store.
     *
     * @return the persistent store coordinator that owns this persistent store
     */
    public PersistentStoreCoordinator getCoordinator() {
        return storeCoordinator;
    }

    /**
     * Returns the location where this persistence store is saved.
     *
     * @return the file where this persistence store is saved
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Returns the store's unique identifier. This is persisted with the store, so it's useful to identify specific
     * store instances. Note that nothing prevents a user from copying a persistence store (and thus it's associated
     * UUID). Therefore, you may technically encounter two stores with the same UUID if one manually made a copy of
     * the store.
     *
     * Store UUIDs are used to identify {@link ManagedObject}
     *
     * @return the store's UUID
     */
    public UUID getUuid() {
        return (UUID) metadata.get(UUID_KEY);
    }

    /**
     * Sets the store's unique identifier. This is used by the Store itself when loading its metadata. Once it's set,
     * it must not change throughout the lifetime of the Store.
     *
     * @param uuid  the UUID to use.
     */
    protected void setUuid(UUID uuid) {
        metadata.put(UUID_KEY, uuid);
    }

    /**
     * Sets the associated persistence store coordinator.
     *
     * @param coordinator the persistence store coordinator to use
     */
    void setPersistentStoreCoordinator(PersistentStoreCoordinator coordinator) {
        storeCoordinator = coordinator;
    }

    /**
     * Creates a new {@link ObjectID}.
     *
     * @param entity            the entity associated with the ObjectID
     * @param referenceObject   the reference object that uniquely identifies the object. This object must implement
     *                          {@link Object#hashCode()} and {@link Object#equals(Object)} appropriately, such that
     *                          for two different instances that represent the same reference object,
     *                          {@code Object#hashCode()} must return the same value and
     *                          {@code Object#equals(Object)} must return true.
     * @return
     */
    ObjectID createObjectID(Entity<?> entity, Object referenceObject) {
        ObjectID id = new ObjectID(this, entity, referenceObject);
        return id;
    }

    /**
     * Returns the associated reference object for the specified ObjectID.
     *
     * @param objectID  the ObjectID to get its reference object
     * @return          the reference object associated with the specified ObjectID
     */
    protected Object getReferenceObjectForObjectID(ObjectID objectID) {
        return objectID.getReferenceObject();
    }

    /**
     * Returns the corresponding ObjectIDs for the specified ManagedObjects. These ObjectIDs must be permanent and must
     * not be changed after this call. Objects that already have a permanent ObjectID must return the same one.
     *
     * @param objects   the list of objects that shall get permanent IDs.
     *
     * @return
     */
    abstract List<ObjectID> getPermanentIDsForObjects(List<ManagedObject> objects);

    abstract StoreCacheNode getObjectValues(ObjectID objectID, ObjectContext context);

    abstract ObjectID getToOneRelationshipValue(ObjectID objectID, Relationship relationship, ObjectContext context);

    abstract Collection<ObjectID> getToManyRelationshipValue(ObjectID objectID, Relationship relationship, ObjectContext context);

    abstract <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest<T> request, ObjectContext context);
    abstract void executeSaveRequest(SaveChangesRequest request, ObjectContext context);

    void contextRegisteredObjectIDs(Collection<ObjectID> objectIDs) {
    }

    void contextUnregisteredObjectIDs(Collection<ObjectID> objectIDs) {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+" [location=" + location + ", UUID=" + getUuid() + "]";
    }
}
