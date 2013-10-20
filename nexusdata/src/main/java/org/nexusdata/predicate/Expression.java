package org.nexusdata.predicate;


public interface Expression<T> {

    public T evaluate(Object object);
}