package org.nexusdata.predicate;

import java.lang.reflect.Field;

import org.nexusdata.core.ManagedObject;
import org.nexusdata.core.ManagedObject;

public class FieldPathExpression implements Expression {

    private final String m_fieldPath;

    public FieldPathExpression(String fieldPath) {
        m_fieldPath = fieldPath;
    }

    public String getFieldPath() {
        return m_fieldPath;
    }

    @Override
    public Object evaluate(Object object) {
        try {
            if (object instanceof ManagedObject) {
                return ((ManagedObject)object).getValue(m_fieldPath);
            } else {
                Field field = object.getClass().getDeclaredField(m_fieldPath);
                field.setAccessible(true);
                return field.get(object);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return m_fieldPath;
    }
}