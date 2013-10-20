package org.nexusdata.predicate.parser;

import org.nexusdata.predicate.Expression;
import org.nexusdata.predicate.FieldPathExpression;
import static org.nexusdata.predicate.parser.PredicateParser.TokenType;

class NameParselet implements PrefixParselet<TokenType,Expression<?>> {
    public Expression<?> parse(Parser<TokenType,Expression<?>> parser, Token<TokenType> token) {
        return new FieldPathExpression(token.getText());
    }
}
