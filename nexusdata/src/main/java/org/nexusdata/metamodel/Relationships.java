package org.nexusdata.metamodel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value=ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Relationships {
    Relationship[] value();

    public @interface Relationship {
        String name();
        Class<?> type();
        RelationshipDescription.Type relationshipType();
        String inverseName();
    }
}
