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
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return value == null ? null : value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstantExpression that = (ConstantExpression) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}