package com.github.dkharrat.nexusdata.predicate.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

class Lexer<T> implements Iterator<Token<T>> {

    private final LexerGrammar<T> grammar;
    private final List<Token<T>> tokens = new LinkedList<Token<T>>();
    private final String text;
    private Iterator<Token<T>> tokenIterator = null;

    Lexer(LexerGrammar<T> grammar, String text) {
        this.grammar = grammar;
        this.text = text;
    }

    private void tokenize() {
        //TODO: make it stream-based, instead of parsing everything

        String s = text;
        tokens.clear();

        boolean foundToken = false;
        while(!s.equals("")) {
            for (LexerGrammar<T>.TokenRule tokenDescriptor : grammar.getRules()) {
                Matcher matcher = tokenDescriptor.getPattern().matcher(s);
                if (matcher.find()) {
                    foundToken = true;
                    String tokenStr = matcher.group().trim();
                    tokens.add(new Token<T>(tokenDescriptor.getType(), tokenStr));

                    s = matcher.replaceFirst("");
                    break;
                }
            }

            if (!foundToken) {
                throw new ParseException("Unexpected token in expression near: " + s);
            }
        }
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Token<T> next() {
        if (tokenIterator == null) {
            tokenize();
            tokenIterator = tokens.iterator();
        }

        if (tokenIterator.hasNext()) {
            return tokenIterator.next();
        } else {
            return new Token<T>(grammar.getEofToken(), null);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

