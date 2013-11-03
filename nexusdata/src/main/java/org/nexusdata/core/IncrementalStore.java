package org.nexusdata.core;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.nexusdata.metamodel.Entity;
import org.nexusdata.metamodel.Relationship;

/**
 * An IncrementalStore represents a persistence store in which data is loaded or saved incrementally as needed. It is
 * typically used when the entire data set cannot fit in-memory, and must be "paged-in" into memory on-demand. This is
 * an abstract class that can be implemented to provide support for such persistence stores.
 */
public abstract class IncrementalStore extends PersistentStore {

    public IncrementalStore(File location) {
        super(location);
    }

    @Override
    protected abstract <T extends ManagedObject>
    List<T> executeFetchRequest(FetchRequest<T> request, ObjectContext context);

    @Override
    protected abstract
    void executeSaveRequest(SaveChangesRequest request, ObjectContext context);

    @Override
    protected abstract StoreCacheNode getObjectValues(ObjectID objectID, ObjectContext context);

    @Override
    protected abstract ObjectID getToOneRelationshipValue(ObjectID objectID, Relationship relationship, ObjectContext context);

    @Override
    protected abstract Collection<ObjectID> getToManyRelationshipValue(ObjectID objectID, Relationship relationship, ObjectContext context);

    @Override
    protected abstract List<ObjectID> getPermanentIDsForObjects(List<ManagedObject> objects);

    @Override
    protected ObjectID createObjectID(Entity<?> entity, Object referenceObject) {
        ObjectID id = new ObjectID(this, entity, referenceObject);
        return id;
    }

    @Override
    public Object getReferenceObjectForObjectID(ObjectID objectID) {
        return super.getReferenceObjectForObjectID(objectID);
    }

    @Override
    protected void contextRegisteredObjectIDs(Collection<ObjectID> objectIDs) {

    }

    @Override
    protected void contextUnregisteredObjectIDs(Collection<ObjectID> objectIDs) {

    }
}
