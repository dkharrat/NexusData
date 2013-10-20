package org.nexusdata.predicate;

import org.nexusdata.utils.ObjectUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ComparisonPredicate implements Predicate {

    public enum Operator {
        EQUAL,
        NOT_EQUAL,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
    }

    private final Expression<?> lhs, rhs;
    private final Operator op;

    public ComparisonPredicate(Expression<?> lhs, Operator op, Expression<?> rhs) {
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    public Expression<?> getLhs() {
        return lhs;
    }

    public Operator getOperator() {
        return op;
    }

    public Expression<?> getRhs() {
        return rhs;
    }

    @Override
    public Boolean evaluate(Object object) {
        Object lhsValue = lhs.evaluate(object);
        Object rhsValue = rhs.evaluate(object);

        if (op == Operator.EQUAL) {
            return ObjectUtil.objectsEqual(lhsValue, rhsValue);
        } else if (op == Operator.NOT_EQUAL) {
            return !ObjectUtil.objectsEqual(lhsValue, rhsValue);
        }

        if (!(lhsValue instanceof Number) || !(rhsValue instanceof Number)) {
            throw new IllegalArgumentException("Cannot compare non-Numbers");
        }
        Number lhsNumber = (Number)lhsValue;
        Number rhsNumber = (Number)rhsValue;

        int comparison = toBigDecimal(lhsNumber).compareTo(toBigDecimal(rhsNumber));

        switch (op) {
            case GREATER_THAN:
                return comparison >= 1;

            case GREATER_THAN_OR_EQUAL:
                return comparison >= 1 || comparison == 0;

            case LESS_THAN:
                return comparison <= -1;

            case LESS_THAN_OR_EQUAL:
                return comparison <= -1 || comparison == 0;

            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
    }

    private static BigDecimal toBigDecimal(final Number number) {
        if(number instanceof BigDecimal)
            return (BigDecimal) number;
        if(number instanceof BigInteger)
            return new BigDecimal((BigInteger) number);
        if(number instanceof Byte || number instanceof Short
                || number instanceof Integer || number instanceof Long)
            return new BigDecimal(number.longValue());
        if(number instanceof Float || number instanceof Double)
            return new BigDecimal(number.doubleValue());

        try {
            return new BigDecimal(number.toString());
        } catch(final NumberFormatException e) {
            throw new RuntimeException("The given number (\"" + number
                    + "\" of class " + number.getClass().getName()
                    + ") does not have a parsable string representation", e);
        }
    }

    @Override
    public String toString() {
        return "(" + lhs + " " + op + " " + rhs + ")";
    }
}