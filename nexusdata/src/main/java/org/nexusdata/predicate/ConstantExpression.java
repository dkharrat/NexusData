package org.nexusdata.predicate;

public class ConstantExpression<T> implements Expression<T> {

    private final T value;

    public ConstantExpression(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public T evaluate(Object object) {
        return getValue();
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}