package org.nexusdata.predicate;


public interface Predicate extends Expression<Boolean> {
    public Boolean evaluate(Object object);
}