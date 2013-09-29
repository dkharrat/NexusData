package org.nexusdata.predicate;

public class ConstantExpression implements Expression {

    private final Object m_value;

    public ConstantExpression(Object value) {
        m_value = value;
    }

    public Object getValue() {
        return m_value;
    }

    @Override
    public Object evaluate(Object object) {
        return getValue();
    }

    @Override
    public String toString() {
        return m_value == null ? null : m_value.toString();
    }
}