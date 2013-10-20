package org.nexusdata.predicate.parser;

interface InfixParselet<T,V> {
    V parse(Parser<T,V> parser, V left, Token<T> token);
    int getPrecedence();
}
