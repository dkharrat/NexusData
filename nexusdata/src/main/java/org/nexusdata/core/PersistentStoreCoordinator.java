package org.nexusdata.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nexusdata.metamodel.Entity;
import org.nexusdata.metamodel.ObjectModel;
import org.nexusdata.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A PersistentStoreCoordinator is a mediator between a {@link ObjectContext} and other {@link PersistentStore}s.
 * It is used by an {@link ObjectContext} to obtain model information and save the object graph. From the perspective of
 * the ObjectContext, a PersistentStoreCoordinator such that a group of PersistentStores appear as one virtual store.
 * This allows the ObjectContext to create the corresponding object graph from the union of the persistence stores that
 * this coordinator covers.
 */
public class PersistentStoreCoordinator {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentStoreCoordinator.class);

    private final Map<UUID, PersistentStore> storeUuidToPersistentStore = new LinkedHashMap<UUID, PersistentStore>();
    private final ObjectModel model;

    /**
     * Creates a PersistentStoreCoordinator associated with the specific model.
     *
     * @param model the model that this PersistentStoreCoordinator will use
     */
    public PersistentStoreCoordinator(ObjectModel model) {
        this.model = model;
    }

    public void addStore(PersistentStore store) {
        if (store.getCoordinator() != null && store.getCoordinator() != this) {
            throw new IllegalStateException("PersistentStore " + store + " already assigned to another coordinator");
        }
        store.setPersistentStoreCoordinator(this);
        store.loadMetadata();

        if (store.getUuid() == null) {
            throw new RuntimeException("Did not get permanent UUID from store: " + store);
        }

        storeUuidToPersistentStore.put(store.getUuid(), store);

        LOG.info("Added persistent store " + store);
    }

    public void removeStore(PersistentStore store) {
        storeUuidToPersistentStore.remove(store.getUuid());
        store.setPersistentStoreCoordinator(null);
    }

    public PersistentStore getPersistentStore(UUID uuid) {
        return storeUuidToPersistentStore.get(uuid);
    }

    public List<PersistentStore> getPersistentStores() {
        return new ArrayList<PersistentStore>(storeUuidToPersistentStore.values());
    }

    public ObjectModel getModel() {
        return model;
    }

    public ObjectID objectIDFromUri(URI objectIDUri) {
        if (!objectIDUri.getScheme().equals("nexusdata")) {
            throw new IllegalArgumentException("");
        } else if (StringUtil.isBlank(objectIDUri.getAuthority())) {
            throw new IllegalArgumentException("Cannot create ObjectID from temporary ID");
        }

        String[] parts = objectIDUri.getPath().split("/");
        if (parts.length < 3 || parts[1].isEmpty() || parts[2].isEmpty()) {
            throw new IllegalArgumentException("Invalid ObjectID URI format");
        }

        UUID storeUuid = UUID.fromString(objectIDUri.getAuthority());
        Entity<?> entity = model.getEntity(parts[1]);
        Object referenceObject = parts[2];
        try {
            referenceObject = Long.parseLong(parts[2]);
        } catch (NumberFormatException e) {
            // ignore; treat ref object as string
        }

        return new ObjectID(storeUuidToPersistentStore.get(storeUuid), entity, referenceObject);
    }

    //TODO: implement - must be synchronized?
    void executeFetchRequest() {

    }

    //TODO: implement - must be synchronized?
    void save(SaveChangesRequest saveRequest) {

    }
}
