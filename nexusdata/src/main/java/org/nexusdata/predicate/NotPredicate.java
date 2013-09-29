package org.nexusdata.predicate;

class NotPredicate implements Predicate {
    private final Predicate m_predicate;

    public NotPredicate(Predicate predicate) {
        m_predicate = predicate;
    }

    public Predicate getPredicate() {
        return m_predicate;
    }

    @Override
    public boolean evaluate(Object object) {
        return !m_predicate.evaluate(object);
    }
}