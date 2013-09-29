package org.nexusdata.predicate;


public class PredicateBuilder {

    private final Predicate curPredicate;
    private final Expression curExpression;

    PredicateBuilder(Predicate curPredicate) {
        this.curPredicate = curPredicate;
        this.curExpression = null;
    }

    PredicateBuilder(Expression curExpression) {
        this.curExpression = curExpression;
        this.curPredicate = null;
    }

    public Predicate getPredicate() {
        return curPredicate;
    }

    Expression getExpression() {
        return curExpression;
    }

    public PredicateBuilder gt(Expression rhs) {
        return new PredicateBuilder(new ComparisonPredicate(curExpression, ComparisonPredicate.Operator.GREATER_THAN, rhs));
    }

    public PredicateBuilder gt(ExpressionBuilder rhs) {
        return gt(rhs.getExpression());
    }

    public PredicateBuilder gt(Object value) {
        return gt(new ConstantExpression(value));
    }

    public PredicateBuilder lt(Expression rhs) {
        return new PredicateBuilder(new ComparisonPredicate(curExpression, ComparisonPredicate.Operator.LESS_THAN, rhs));
    }

    public PredicateBuilder lt(ExpressionBuilder rhs) {
        return lt(rhs.getExpression());
    }

    public PredicateBuilder lt(Object value) {
        return lt(new ConstantExpression(value));
    }

    public PredicateBuilder eq(Expression rhs) {
        return new PredicateBuilder(new ComparisonPredicate(curExpression, ComparisonPredicate.Operator.EQUAL, rhs));
    }

    public PredicateBuilder eq(ExpressionBuilder rhs) {
        return eq(rhs.getExpression());
    }

    public PredicateBuilder eq(Object value) {
        return eq(new ConstantExpression(value));
    }

    public PredicateBuilder and(PredicateBuilder rhs) {
        return new PredicateBuilder(new CompoundPredicate(curPredicate, CompoundPredicate.Operator.AND, rhs.getPredicate()));
    }

    public PredicateBuilder or(PredicateBuilder rhs) {
        return new PredicateBuilder(new CompoundPredicate(curPredicate, CompoundPredicate.Operator.OR, rhs.getPredicate()));
    }
}