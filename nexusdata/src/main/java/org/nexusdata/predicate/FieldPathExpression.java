package org.nexusdata.predicate;

import java.lang.reflect.Field;

import org.nexusdata.core.ManagedObject;

public class FieldPathExpression implements Expression<Object> {

    private final String fieldPath;

    public FieldPathExpression(String fieldPath) {
        this.fieldPath = fieldPath;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Object evaluate(Object object) {
        try {
            if (object instanceof ManagedObject) {
                return ((ManagedObject)object).getValue(fieldPath);
            } else {
                Field field = object.getClass().getDeclaredField(fieldPath);
                field.setAccessible(true);
                return field.get(object);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return fieldPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldPathExpression that = (FieldPathExpression) o;

        if (!fieldPath.equals(that.fieldPath)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return fieldPath.hashCode();
    }
}