package com.github.dkharrat.nexusdata.predicate;

public class ThisExpression implements Expression<Object> {

    @Override
    public Object evaluate(Object object) {
        return object;
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "this";
    }
}