package org.nexusdata.predicate.parser;

import org.nexusdata.predicate.ComparisonPredicate;
import static org.nexusdata.predicate.ComparisonPredicate.Operator;
import org.nexusdata.predicate.Expression;

import static org.nexusdata.predicate.parser.PredicateParser.TokenType;

public class ComparisonParselet implements InfixParselet<TokenType,Expression<?>> {
    private final Operator operator;
    private final int precedence;

    ComparisonParselet(Operator operator, int precedence) {
        this.operator = operator;
        this.precedence = precedence;
    }

    public Expression<?> parse(Parser<TokenType,Expression<?>> parser, Expression<?> left, Token<TokenType> token) {

        Expression<?> right = parser.parse(getPrecedence() - 1);

        return new ComparisonPredicate(left, operator, right);
    }

    public int getPrecedence() {
        return precedence;
    }
}
