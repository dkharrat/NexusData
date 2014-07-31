package com.github.dkharrat.nexusdata.predicate;


import com.github.dkharrat.nexusdata.predicate.parser.PredicateParser;

public class PredicateBuilder {

    private final Predicate curPredicate;
    private final Expression<?> curExpression;

    PredicateBuilder(Predicate curPredicate) {
        this.curPredicate = curPredicate;
        this.curExpression = null;
    }

    PredicateBuilder(Expression<?> curExpression) {
        this.curExpression = curExpression;
        this.curPredicate = null;
    }

    public Predicate getPredicate() {
        return curPredicate;
    }

    public static Predicate parse(String expr) {
        PredicateParser parser = new PredicateParser(expr);
        return parser.parse();
    }

    Expression<?> getExpression() {
        return curExpression;
    }

    public PredicateBuilder gt(Expression<?> rhs) {
        return new PredicateBuilder(new ComparisonPredicate(curExpression, ComparisonPredicate.Operator.GREATER_THAN, rhs));
    }

    public PredicateBuilder gt(ExpressionBuilder rhs) {
        return gt(rhs.getExpression());
    }

    public <T> PredicateBuilder gt(T value) {
        return gt(new ConstantExpression<T>(value));
    }

    public PredicateBuilder lt(Expression rhs) {
        return new PredicateBuilder(new ComparisonPredicate(curExpression, ComparisonPredicate.Operator.LESS_THAN, rhs));
    }

    public PredicateBuilder lt(ExpressionBuilder rhs) {
        return lt(rhs.getExpression());
    }

    public <T> PredicateBuilder lt(T value) {
        return lt(new ConstantExpression<T>(value));
    }

    public PredicateBuilder eq(Expression<?> rhs) {
        return new PredicateBuilder(new ComparisonPredicate(curExpression, ComparisonPredicate.Operator.EQUAL, rhs));
    }

    public PredicateBuilder eq(ExpressionBuilder rhs) {
        return eq(rhs.getExpression());
    }

    public <T> PredicateBuilder eq(T value) {
        return eq(new ConstantExpression<T>(value));
    }

    public PredicateBuilder notEq(Expression<?> rhs) {
        return new PredicateBuilder(new ComparisonPredicate(curExpression, ComparisonPredicate.Operator.NOT_EQUAL, rhs));
    }

    public PredicateBuilder notEq(ExpressionBuilder rhs) {
        return notEq(rhs.getExpression());
    }

    public <T> PredicateBuilder notEq(T value) {
        return notEq(new ConstantExpression<T>(value));
    }

    public PredicateBuilder isNull() {
        return eq(new ConstantExpression<Object>(null));
    }

    public PredicateBuilder isNotNull() {
        return notEq(new ConstantExpression<Object>(null));
    }

    public PredicateBuilder and(PredicateBuilder rhs) {
        return new PredicateBuilder(new CompoundPredicate(curPredicate, CompoundPredicate.Operator.AND, rhs.getPredicate()));
    }

    public PredicateBuilder or(PredicateBuilder rhs) {
        return new PredicateBuilder(new CompoundPredicate(curPredicate, CompoundPredicate.Operator.OR, rhs.getPredicate()));
    }
}