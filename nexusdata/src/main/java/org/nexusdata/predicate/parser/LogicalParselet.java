package org.nexusdata.predicate.parser;

import org.nexusdata.predicate.CompoundPredicate;
import org.nexusdata.predicate.Expression;
import org.nexusdata.predicate.Predicate;
import static org.nexusdata.predicate.CompoundPredicate.Operator;
import static org.nexusdata.predicate.parser.PredicateParser.TokenType;

public class LogicalParselet implements InfixParselet<TokenType,Expression<?>> {

    private final Operator operator;
    private final int precedence;

    LogicalParselet(Operator operator, int precedence) {
        this.operator = operator;
        this.precedence = precedence;
    }

    public Expression<?> parse(Parser<TokenType,Expression<?>> parser, Expression<?> left, Token<TokenType> token) {

        if (!(left instanceof Predicate)) {
            throw new ParseException("Expected a predicate for left-hand side, but got an expression");
        }

        Expression<?> right = parser.parse(getPrecedence() - 1);

        if (!(right instanceof Predicate)) {
            throw new ParseException("Expected a predicate for right-hand side, but got an expression");
        }

        return new CompoundPredicate((Predicate)left, operator, (Predicate)right);
    }

    public int getPrecedence() {
        return precedence;
    }
}
