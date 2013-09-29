package org.nexusdata.predicate;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ComparisonPredicate implements Predicate {

    public enum Operator {
        EQUAL,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
    }

    private final Expression m_lhs, m_rhs;
    private final Operator m_op;

    public ComparisonPredicate(Expression lhs, Operator op, Expression rhs) {
        m_lhs = lhs;
        m_op = op;
        m_rhs = rhs;
    }

    public Expression getLhs() {
        return m_lhs;
    }

    public Operator getOperator() {
        return m_op;
    }

    public Expression getRhs() {
        return m_rhs;
    }

    @Override
    public boolean evaluate(Object object) {
        Object lhsValue = m_lhs.evaluate(object);
        Object rhsValue = m_rhs.evaluate(object);

        if (m_op == Operator.EQUAL) {
            if (lhsValue == null) {
                return rhsValue == null;
            } else {
                return lhsValue.equals(rhsValue);
            }
        }

        if (!(lhsValue instanceof Number) || !(rhsValue instanceof Number)) {
            throw new IllegalArgumentException("Cannot compare non-Numbers");
        }
        Number lhsNumber = (Number)lhsValue;
        Number rhsNumber = (Number)rhsValue;

        int comparison = toBigDecimal(lhsNumber).compareTo(toBigDecimal(rhsNumber));

        switch (m_op) {
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
        return "(" + m_lhs + " " + m_op + " " + m_rhs + ")";
    }
}