package org.nexusdata.predicate;

public class ConstantExpression implements Expression {

    private final Object value;

    public ConstantExpression(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Object evaluate(Object object) {
        return getValue();
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }
}