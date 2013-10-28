package org.nexusdata.core;

import java.util.ArrayList;
import java.util.List;

import org.nexusdata.metamodel.Entity;
import org.nexusdata.predicate.Predicate;

public class FetchRequest<T extends ManagedObject> extends PersistentStoreRequest {

    private final Entity<T> entity;
    private Predicate predicate;
    private final List<SortDescriptor> sortDescriptors = new ArrayList<SortDescriptor>();
    private int limit = Integer.MAX_VALUE;
    private int offset = 0;
    private boolean includesPendingChanges = true;
    private boolean returnsObjectsAsFaults = true;

    public FetchRequest(Entity<T> entity) {
        this.entity = entity;
    }

    public Entity<T> getEntity() {
        return entity;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public List<SortDescriptor> getSortDescriptors() {
        return sortDescriptors;
    }

    public boolean hasSortDescriptors() {
        return !sortDescriptors.isEmpty();
    }

    public void setSortDescriptors(List<SortDescriptor> sortDescriptors) {
        this.sortDescriptors.clear();
        this.sortDescriptors.addAll(sortDescriptors);
    }

    public void setSortDescriptor(SortDescriptor sortDescriptor) {
        sortDescriptors.clear();
        sortDescriptors.add(sortDescriptor);
    }

    public void addSortDescriptor(SortDescriptor sortDescriptor) {
        sortDescriptors.add(sortDescriptor);
    }

    public boolean includesPendingChanges() {
        return includesPendingChanges;
    }

    public void setIncludePendingChanges(boolean includePendingChanges) {
        includesPendingChanges = includePendingChanges;
    }

    public boolean returnsObjectsAsFaults() {
        return returnsObjectsAsFaults;
    }

    public void setReturnObjectsAsFaults(boolean returnObjectsAsFaults) {
        returnsObjectsAsFaults = returnObjectsAsFaults;
    }

    public static class Builder<T extends ManagedObject> {
        private final FetchRequest<T> fetchRequest;

        public static <S extends ManagedObject> Builder<S> forEntity(Entity<S> entity) {
            return new Builder<S>(new FetchRequest<S>(entity));
        }

        private Builder(FetchRequest<T> fetchRequest) {
            this.fetchRequest = fetchRequest;
        }

        public Builder<T> predicate(Predicate predicate) {
            fetchRequest.setPredicate(predicate);
            return this;
        }

        public Builder<T> sortBy(String name, boolean ascending) {
            fetchRequest.addSortDescriptor(new SortDescriptor(name, ascending));
            return this;
        }

        public Builder<T> includePendingChanges(boolean includePendingChanges) {
            fetchRequest.setIncludePendingChanges(includePendingChanges);
            return this;
        }

        public Builder<T> limit(int limit) {
            fetchRequest.setLimit(limit);
            return this;
        }

        public FetchRequest<T> build() {
            return fetchRequest;
        }
    }

    @Override
    public String toString() {
        return "FetchRequest{" +
                "entity=" + entity.getName() +
                ", predicate=" + predicate +
                ", sortDescriptors=" + sortDescriptors +
                ", limit=" + limit +
                ", offset=" + offset +
                '}';
    }
}