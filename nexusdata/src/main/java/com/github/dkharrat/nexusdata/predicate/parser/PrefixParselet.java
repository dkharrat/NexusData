package com.github.dkharrat.nexusdata.predicate.parser;

interface PrefixParselet<T,V> {
    V parse(Parser<T,V> parser, Token<T> token);
}
