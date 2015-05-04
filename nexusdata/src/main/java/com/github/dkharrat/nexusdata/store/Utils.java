package com.github.dkharrat.nexusdata.store;

import com.github.dkharrat.nexusdata.metamodel.Entity;
import com.github.dkharrat.nexusdata.metamodel.Property;

import java.util.*;

class Utils {
    static Collection<Entity<?>> getAllChildEntities(Entity<?> entity, Collection<Entity<?>> childEntities) {
        childEntities.addAll(entity.getSubEntities());
        for (Entity<?> subEntity : entity.getSubEntities()) {
            getAllChildEntities(subEntity, childEntities);
        }
        return childEntities;
    }

    static Set<Property> getPropertiesOfEntityAndItsChildren(Entity<?> entity) {
        Set<Property> properties = new LinkedHashSet<>();

        properties.addAll(entity.getProperties());
        Collection<Entity<?>> childEntities = getAllChildEntities(entity, new ArrayList<Entity<?>>());
        for (Entity<?> childEntity : childEntities) {
            properties.addAll(childEntity.getProperties());
        }

        return properties;
    }
}
