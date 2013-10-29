package org.nexusdata.core;

import java.util.ArrayList;
import java.util.List;

import org.nexusdata.metamodel.Entity;
import org.nexusdata.predicate.Predicate;
import org.nexusdata.predicate.PredicateBuilder;

/**
 * Describes query to the persistence store to retrieve all the objects that match the specified criteria.
 * A FetchRequest is passed to an {@link ObjectContext} to handle the query. Each FetchRequest is
 * tied to a specific {@link Entity} type, meaning that only objects of the specified entity type are returned.
 */
public class FetchRequest<T extends ManagedObject> implements PersistentStoreRequest {

    private final Entity<T> entity;
    private Predicate predicate;
    private final List<SortDescriptor> sortDescriptors = new ArrayList<SortDescriptor>();
    private int limit = Integer.MAX_VALUE;
    private int offset = 0;
    private boolean includesPendingChanges = true;
    private boolean returnsObjectsAsFaults = true;

    /**
     * Creates a new FetchRequest for a specific entity type. Only objects of a type that matches
     * the specified entity are returned.
     *
     * @param entity The entity to query for
     */
    public FetchRequest(Entity<T> entity) {
        this.entity = entity;
    }

    /**
     * Returns the entity of this FetchRequest
     *
     * @return the entity of this FetchRequest
     */
    public Entity<T> getEntity() {
        return entity;
    }

    /**
     * Returns the predicate used for this FetchRequest
     *
     * @return the predicate used for this FetchRequest
     */
    public Predicate getPredicate() {
        return predicate;
    }

    /**
     * Sets the predicate to use for this FetchRequest
     *
     * @param predicate the predicate to use
     */
    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    /**
     * Returns the maximum number of objects to return when this fetch request is executed
     *
     * @return the maximum number of objects to return
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Sets the maximum number of objects to return when this fetch request is executed. The default is to return
     * everything.
     *
     * @param limit an integer specifying
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Returns the index of the list of results to skip to when this fetch request is executed. The default offset is 0.
     *
     * @return Returns the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the index of the list of results to skip to when this fetch request is executed.
     *
     * @param offset The offset that specified the first set of the objects to skip
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Returns the list of sort descriptors used by this fetch request
     *
     * @return the list of sort descriptors used by this fetch request
     */
    public List<SortDescriptor> getSortDescriptors() {
        return sortDescriptors;
    }

    /**
     * Returns true if this fetch request specifies any sort descriptors.
     *
     * @return true if this fetch request specifies any sort descriptors, or false otherwise.
     */
    public boolean hasSortDescriptors() {
        return !sortDescriptors.isEmpty();
    }

    /**
     * Sets the list of sort descriptors to use for this fetch request. This method removes any existing sort
     * descriptors, if any, prior to setting the sort descriptors. A SortDescriptor specifies the type of sorting to perform on the results returned when this
     * fetch request is executed by the persistence store. The order of the SortDescriptor in the list specifies the
     * precedence to use when sorting. For example, if
     * <code>sortDescriptors = List(SortDescriptor("name", true), SortDescriptor("age", false))</code>, then the results
     * are first sorted by name in ascending order, and then for duplicate names, they are sorted by age in descending
     * order.
     *
     * @param sortDescriptors   the list of sort descriptors to use
     */
    public void setSortDescriptors(List<SortDescriptor> sortDescriptors) {
        this.sortDescriptors.clear();
        this.sortDescriptors.addAll(sortDescriptors);
    }

    /**
     * Sets the sort descriptor to use for this fetch request. This method removes any existing sort descriptors, if
     * any prior to setting the sort descriptor.
     *
     * @param sortDescriptor    the sort descriptor to use
     */
    public void setSortDescriptor(SortDescriptor sortDescriptor) {
        sortDescriptors.clear();
        sortDescriptors.add(sortDescriptor);
    }

    /**
     * Adds a sort descriptor to the existing sort descriptors, if any. The list of sort descriptors in a fetch request
     * maintain their order of insertion.
     *
     * @param sortDescriptor the sort descriptor to add
     */
    public void addSortDescriptor(SortDescriptor sortDescriptor) {
        sortDescriptors.add(sortDescriptor);
    }

    /**
     * Indicates whether this fetch request will cover objects that have pending changes in the {@link ObjectContext}
     * but haven't been persisted yet. Default is true.
     *
     * @return true if pending changes will be covered by the query, or false otherwise
     */
    public boolean includesPendingChanges() {
        return includesPendingChanges;
    }

    /**
     * Sets whether this fetch request will cover objects that have pending changes in the {@link ObjectContext}
     * but haven't been persisted yet.
     *
     * @param includePendingChanges if true, pending changes will be covered by the query. Otherwise, the query will
     *                              only cover the objects as reflected by the persistence store.
     */
    public void setIncludePendingChanges(boolean includePendingChanges) {
        includesPendingChanges = includePendingChanges;
    }

    /**
     * Indicates whether the objects returned from the fetch request will be returned as faults. Default is true.
     *
     * @return  true if objects will be returned as faults, or false otherwise.
     */
    public boolean returnsObjectsAsFaults() {
        return returnsObjectsAsFaults;
    }

    /**
     * Sets whether the objects returned from the fetch request will be returned as faults.
     *
     * @param returnObjectsAsFaults if true, objects will be returned as faults. Otherwise, they will be returned fully materialized with
     *          their property values initialized. See {@link ObjectContext} for a discussion about faulting.
     */
    public void setReturnObjectsAsFaults(boolean returnObjectsAsFaults) {
        returnsObjectsAsFaults = returnObjectsAsFaults;
    }

    @Override
    public String toString() {
        return "FetchRequest{" +
                "entity=" + entity.getName() +
                ", predicate=" + predicate +
                ", sortDescriptors=" + sortDescriptors +
                ", limit=" + (limit == Integer.MAX_VALUE ? "MAX" : limit) +
                ", offset=" + offset +
                '}';
    }

    /**
     * A builder class that simplifies the creation of a FetchRequest.
     */
    public static class Builder<T extends ManagedObject> {
        private final FetchRequest<T> fetchRequest;

        /**
         * @see FetchRequest#FetchRequest(org.nexusdata.metamodel.Entity)
         */
        public static <S extends ManagedObject> Builder<S> forEntity(Entity<S> entity) {
            return new Builder<S>(new FetchRequest<S>(entity));
        }

        private Builder(FetchRequest<T> fetchRequest) {
            this.fetchRequest = fetchRequest;
        }

        /**
         * @see FetchRequest#setPredicate(org.nexusdata.predicate.Predicate)
         */
        public Builder<T> predicate(Predicate predicate) {
            fetchRequest.setPredicate(predicate);
            return this;
        }

        /**
         * Sets the predicate given a string representation.
         *
         * @see FetchRequest#setPredicate(org.nexusdata.predicate.Predicate)
         */
        public Builder<T> predicate(String predicateToParse) {
            fetchRequest.setPredicate(PredicateBuilder.parse(predicateToParse));
            return this;
        }

        /**
         * @see FetchRequest#addSortDescriptor(SortDescriptor)
         */
        public Builder<T> sortBy(String name, boolean ascending) {
            fetchRequest.addSortDescriptor(new SortDescriptor(name, ascending));
            return this;
        }

        /**
         * @see FetchRequest#setIncludePendingChanges(boolean)
         */
        public Builder<T> includePendingChanges(boolean includePendingChanges) {
            fetchRequest.setIncludePendingChanges(includePendingChanges);
            return this;
        }

        /**
         * @see FetchRequest#setLimit(int)
         */
        public Builder<T> limit(int limit) {
            fetchRequest.setLimit(limit);
            return this;
        }

        /**
         * @see FetchRequest#setOffset(int)
         */
        public Builder<T> offset(int offset) {
            fetchRequest.setOffset(offset);
            return this;
        }

        /**
         * Returns a constructed FetchRequest from this builder.
         * @return
         */
        public FetchRequest<T> build() {
            return fetchRequest;
        }
    }
}