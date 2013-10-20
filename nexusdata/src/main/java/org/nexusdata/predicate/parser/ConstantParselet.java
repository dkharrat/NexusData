package org.nexusdata.predicate.parser;

import org.nexusdata.predicate.ConstantExpression;
import org.nexusdata.predicate.Expression;

import static org.nexusdata.predicate.parser.PredicateParser.TokenType;

public class ConstantParselet implements PrefixParselet<TokenType,Expression<?>> {
    @Override
    public Expression<?> parse(Parser<TokenType,Expression<?>> parser, Token<TokenType> token) {

        String valueStr = token.getText();
        Object value;

        if (valueStr.equals("true")) {
            value = true;
        } else if (valueStr.equals("false")) {
            value = false;
        } else {
            value = Integer.parseInt(valueStr);
        }

        return new ConstantExpression(value);
    }
}
