package org.nexusdata.predicate;


public class CompoundPredicate implements Predicate {
    public enum Operator {
        AND,
        OR,
    }

    private final Predicate m_lhs, m_rhs;
    private final Operator m_op;

    public CompoundPredicate(Predicate lhs, Operator op, Predicate rhs) {
        m_lhs = lhs;
        m_op = op;
        m_rhs = rhs;
    }

    public Predicate getLhs() {
        return m_lhs;
    }

    public Operator getOperator() {
        return m_op;
    }

    public Predicate getRhs() {
        return m_rhs;
    }

    @Override
    public boolean evaluate(Object object) {
        boolean lhsValue = m_lhs.evaluate(object);
        boolean rhsValue = m_rhs.evaluate(object);

        switch (m_op) {
            case AND:
                return lhsValue && rhsValue;
            case OR:
                return lhsValue || rhsValue;
            default:
                throw new UnsupportedOperationException("Unsupported compound operator: " + m_op);
        }
    }

    @Override
    public String toString() {
        return "(" + m_lhs + " " + m_op + " " + m_rhs + ")";
    }
}