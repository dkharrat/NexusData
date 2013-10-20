package org.nexusdata.predicate.parser;

import org.nexusdata.predicate.Expression;

import static org.nexusdata.predicate.parser.PredicateParser.TokenType;

public class GroupParselet implements PrefixParselet<TokenType,Expression<?>> {
    @Override
    public Expression<?> parse(Parser<TokenType,Expression<?>> parser, Token<TokenType> token) {
        Expression<?> predicate = parser.parse();
        parser.consume(TokenType.CLOSE_PAREN);
        return predicate;
    }
}
