package com.github.dkharrat.nexusdata.predicate;

public interface ExpressionVisitor<T> {
    public T visit(ConstantExpression<?> expression);
    public T visit(FieldPathExpression expression);
    public T visit(CompoundPredicate predicate);
    public T visit(ComparisonPredicate predicate);
    public T visit(NotPredicate predicate);
}
