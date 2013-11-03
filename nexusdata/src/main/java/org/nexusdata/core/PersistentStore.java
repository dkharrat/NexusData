package org.nexusdata.core;

import java.io.File;
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
    private final File location;
    private final Map<String,Object> metadata = new HashMap<String,Object>();

    PersistentStore(File location) {
        this.location = location;
    }

    protected abstract void loadMetadata();

    public PersistentStoreCoordinator getCoordinator() {
        return storeCoordinator;
    }

    public File getLocation() {
        return location;
    }

    public UUID getUuid() {
        return (UUID) metadata.get(UUID_KEY);
    }

    protected void setUuid(UUID uuid) {
        metadata.put(UUID_KEY, uuid);
    }

    void setPersistentStoreCoordinator(PersistentStoreCoordinator coordinator) {
        storeCoordinator = coordinator;
    }

    ObjectID createObjectID(Entity<?> entity, Object referenceObject) {
        ObjectID id = new ObjectID(this, entity, referenceObject);
        return id;
    }

    protected Object getReferenceObjectForObjectID(ObjectID objectID) {
        return objectID.getReferenceObject();
    }

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
