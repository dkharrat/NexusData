package org.nexusdata.predicate;


public interface Expression<T> {

    public <V> V accept(ExpressionVisitor<V> visitor);
    public T evaluate(Object object);
}