package org.nexusdata.predicate;


public class ExpressionBuilder {
    private final Expression curExpression;

    ExpressionBuilder(Expression curExp) {
        curExpression = curExp;
    }

    public Expression getExpression() {
        return curExpression;
    }

    public static <T> PredicateBuilder constant(T value) {
        return new PredicateBuilder(new ConstantExpression<T>(value));
    }

    public static PredicateBuilder field(String fieldPath) {
        return new PredicateBuilder(new FieldPathExpression(fieldPath));
    }
}