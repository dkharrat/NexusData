package com.github.dkharrat.nexusdata.predicate.parser;

import java.util.*;

/**
 * Generic and extensible parser for a string. It works by registering a "Parselet" that defines parsing
 * behavior for a specific token.
 * <p>
 * Implementation is based on <a href="http://journal.stuffwithstuff.com/2011/03/19/pratt-parsers-expression-parsing-made-easy/">Bob Nystrom's post</a>.
 *
 * @param <T>   The token type
 * @param <V>   The return type of the parser
 */
class Parser<T,V> {
    private final Map<T,PrefixParselet<T,V>> prefixParselets = new HashMap<T, PrefixParselet<T,V>>();
    private final Map<T,InfixParselet<T,V>> infixParselets = new HashMap<T, InfixParselet<T,V>>();
    private final Iterator<Token<T>> tokens;
    private final List<Token<T>> read = new ArrayList<Token<T>>();

    public Parser(Iterator<Token<T>> tokens) {
        this.tokens = tokens;
    }

    public V parse(int precedence) {
        Token<T> token = consume();
        PrefixParselet<T,V> prefix = prefixParselets.get(token.getType());

        if (prefix == null) {
            throw new ParseException("Could not parse \"" + token.getText() + "\".");
        }

        V left = prefix.parse(this, token);

        while (precedence < getPrecedence()) {
            token = consume();

            InfixParselet<T,V> infix = infixParselets.get(token.getType());
            left = infix.parse(this, left, token);
        }

        return left;
    }

    public V parse() {
        return parse(0);
    }

    public void registerParslets(T tokenType, PrefixParselet<T,V> prefixParselet) {
        prefixParselets.put(tokenType, prefixParselet);
    }

    public void registerParslets(T tokenType, InfixParselet<T,V> infixParselet) {
        infixParselets.put(tokenType, infixParselet);
    }

    public Token<T> consume() {
        lookAhead(0);
        return read.remove(0);
    }

    public Token<T> consume(T expectedToken) {
        Token<T> token = lookAhead(0);
        if (token.getType() != expectedToken) {
            throw new ParseException("Expected token " + expectedToken + ", but found " + token);
        }
        return consume();
    }

    private Token<T> lookAhead(int distance) {
        while (read.size() <= distance) {
            read.add(tokens.next());
        }
        return read.get(distance);
    }

    private int getPrecedence() {
        InfixParselet parser = infixParselets.get(lookAhead(0).getType());
        if (parser != null) return parser.getPrecedence();

        return 0;
    }
}

