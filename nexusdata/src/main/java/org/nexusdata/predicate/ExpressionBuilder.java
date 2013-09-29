package org.nexusdata.predicate;


public class ExpressionBuilder {
    private final Expression curExpression;

    ExpressionBuilder(Expression curExp) {
        curExpression = curExp;
    }

    public Expression getExpression() {
        return curExpression;
    }

    public static PredicateBuilder constant(Object value) {
        return new PredicateBuilder(new ConstantExpression(value));
    }

    public static PredicateBuilder field(String fieldPath) {
        return new PredicateBuilder(new FieldPathExpression(fieldPath));
    }
}