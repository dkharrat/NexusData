package org.nexusdata.core;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.metamodel.RelationshipDescription;

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
    protected abstract ObjectID getToOneRelationshipValue(ObjectID objectID, RelationshipDescription relationship, ObjectContext context);

    @Override
    protected abstract Collection<ObjectID> getToManyRelationshipValue(ObjectID objectID, RelationshipDescription relationship, ObjectContext context);

    @Override
    protected abstract List<ObjectID> getPermanentIDsForObjects(List<ManagedObject> objects);

    @Override
    protected ObjectID createObjectID(EntityDescription<?> entity, Object referenceObject) {
        ObjectID id = new ObjectID(this, entity, referenceObject);
        return id;
    }

    @Override
    protected void contextRegisteredObjectIDs(Collection<ObjectID> objectIDs) {

    }

    @Override
    protected void contextUnregisteredObjectIDs(Collection<ObjectID> objectIDs) {

    }
}
