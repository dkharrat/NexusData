package org.nexusdata.core;

import java.util.ArrayList;
import java.util.List;

import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.predicate.Predicate;
import org.nexusdata.metamodel.EntityDescription;
import org.nexusdata.predicate.Predicate;

public class FetchRequest<T extends ManagedObject> extends PersistentStoreRequest {

    private final EntityDescription<T> m_entity;
    private Predicate m_predicate;
    private final List<SortDescriptor> m_sortDescriptors = new ArrayList<SortDescriptor>();
    private int m_limit = Integer.MAX_VALUE;
    private boolean m_includesPendingChanges = true;
    private boolean m_returnsObjectsAsFaults = true;

    public FetchRequest(EntityDescription<T> entity) {
        m_entity = entity;
    }

    public EntityDescription<T> getEntity() {
        return m_entity;
    }

    public Predicate getPredicate() {
        return m_predicate;
    }

    public void setPredicate(Predicate predicate) {
        m_predicate = predicate;
    }

    public int getLimit() {
        return m_limit;
    }

    public void setLimit(int limit) {
        m_limit = limit;
    }

    public List<SortDescriptor> getSortDescriptors() {
        return m_sortDescriptors;
    }

    public boolean hasSortDescriptors() {
        return !m_sortDescriptors.isEmpty();
    }

    public void setSortDescriptors(List<SortDescriptor> sortDescriptors) {
        m_sortDescriptors.clear();
        m_sortDescriptors.addAll(sortDescriptors);
    }

    public void setSortDescriptor(SortDescriptor sortDescriptor) {
        m_sortDescriptors.clear();
        m_sortDescriptors.add(sortDescriptor);
    }

    public void addSortDescriptor(SortDescriptor sortDescriptor) {
        m_sortDescriptors.add(sortDescriptor);
    }

    public boolean includesPendingChanges() {
        return m_includesPendingChanges;
    }

    public void setIncludePendingChanges(boolean includePendingChanges) {
        m_includesPendingChanges = includePendingChanges;
    }

    public boolean returnsObjectsAsFaults() {
        return m_returnsObjectsAsFaults;
    }

    public void setReturnObjectsAsFaults(boolean returnObjectsAsFaults) {
        m_returnsObjectsAsFaults = returnObjectsAsFaults;
    }

    public static class Builder<T extends ManagedObject> {
        private final FetchRequest<T> m_fetchRequest;

        public static <S extends ManagedObject> Builder<S> forEntity(EntityDescription<S> entity) {
            return new Builder<S>(new FetchRequest<S>(entity));
        }

        private Builder(FetchRequest<T> fetchRequest) {
            m_fetchRequest = fetchRequest;
        }

        public Builder<T> predicate(Predicate predicate) {
            m_fetchRequest.setPredicate(predicate);
            return this;
        }

        public Builder<T> sortBy(String name, boolean ascending) {
            m_fetchRequest.addSortDescriptor(new SortDescriptor(name, ascending));
            return this;
        }

        public Builder<T> includePendingChanges(boolean includePendingChanges) {
            m_fetchRequest.setIncludePendingChanges(includePendingChanges);
            return this;
        }

        public Builder<T> limit(int limit) {
            m_fetchRequest.setLimit(limit);
            return this;
        }

        public FetchRequest<T> build() {
            return m_fetchRequest;
        }
    }
}