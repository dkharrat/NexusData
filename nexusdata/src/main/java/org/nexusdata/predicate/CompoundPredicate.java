package org.nexusdata.predicate;


public class CompoundPredicate implements Predicate {
    public static enum Operator {
        AND,
        OR,
    }

    private final Predicate lhs, rhs;
    private final Operator op;

    public CompoundPredicate(Predicate lhs, Operator op, Predicate rhs) {
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    public Predicate getLhs() {
        return lhs;
    }

    public Operator getOperator() {
        return op;
    }

    public Predicate getRhs() {
        return rhs;
    }

    @Override
    public Boolean evaluate(Object object) {
        boolean lhsValue = lhs.evaluate(object);
        boolean rhsValue = rhs.evaluate(object);

        switch (op) {
            case AND:
                return lhsValue && rhsValue;
            case OR:
                return lhsValue || rhsValue;
            default:
                throw new UnsupportedOperationException("Unsupported compound operator: " + op);
        }
    }

    @Override
    public String toString() {
        return "(" + lhs + " " + op + " " + rhs + ")";
    }
}