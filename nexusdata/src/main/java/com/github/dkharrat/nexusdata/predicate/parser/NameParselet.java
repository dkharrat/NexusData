package com.github.dkharrat.nexusdata.predicate.parser;

import com.github.dkharrat.nexusdata.predicate.Expression;
import com.github.dkharrat.nexusdata.predicate.FieldPathExpression;
import static com.github.dkharrat.nexusdata.predicate.parser.PredicateParser.TokenType;

class NameParselet implements PrefixParselet<TokenType,Expression<?>> {
    public Expression<?> parse(Parser<TokenType,Expression<?>> parser, Token<TokenType> token) {
        return new FieldPathExpression(token.getText());
    }
}
