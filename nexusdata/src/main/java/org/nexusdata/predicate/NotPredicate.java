package org.nexusdata.predicate;

class NotPredicate implements Predicate {
    private final Predicate predicate;

    public NotPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public boolean evaluate(Object object) {
        return !predicate.evaluate(object);
    }
}