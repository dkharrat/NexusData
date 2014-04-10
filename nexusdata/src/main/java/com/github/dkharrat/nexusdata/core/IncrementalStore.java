package com.github.dkharrat.nexusdata.core;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

import com.github.dkharrat.nexusdata.metamodel.Entity;
import com.github.dkharrat.nexusdata.metamodel.Relationship;

/**
 * An IncrementalStore represents a persistence store in which data is loaded or saved incrementally as needed. It is
 * typically used when the entire data set cannot fit in-memory, and must be "paged-in" into memory on-demand. This is
 * an abstract class that can be implemented to provide support for such persistence stores.
 */
public abstract class IncrementalStore extends PersistentStore {

    public IncrementalStore(URL location) {
        super(location);
    }

    public IncrementalStore(File location) {
        super(location);
    }

    @Override
    protected abstract <T extends ManagedObject>
    List<T> executeFetchRequest(FetchRequest<T> request, ObjectContext context);

    @Override
    protected abstract void executeSaveRequest(SaveChangesRequest request, ObjectContext context);

    @Override
    protected abstract StoreCacheNode getObjectValues(ObjectID objectID, ObjectContext context);

    @Override
    protected abstract ObjectID getToOneRelationshipValue(ObjectID objectID, Relationship relationship, ObjectContext context);

    @Override
    protected abstract Set<ObjectID> getToManyRelationshipValue(ObjectID objectID, Relationship relationship, ObjectContext context);

    @Override
    protected abstract List<ObjectID> getPermanentIDsForObjects(List<ManagedObject> objects);

    @Override
    protected ObjectID createObjectID(Entity<?> entity, Object referenceObject) {
        return super.createObjectID(entity, referenceObject);
    }

    @Override
    public Object getReferenceObjectForObjectID(ObjectID objectID) {
        return super.getReferenceObjectForObjectID(objectID);
    }
}
