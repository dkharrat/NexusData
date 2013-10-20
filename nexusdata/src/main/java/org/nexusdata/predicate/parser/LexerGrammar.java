package org.nexusdata.predicate.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

class LexerGrammar<T> {
    class TokenRule {
        private final Pattern pattern;
        private final T type;

        TokenRule(Pattern pattern, T type) {
            this.pattern = pattern;
            this.type = type;
        }

        Pattern getPattern() {
            return pattern;
        }

        T getType() {
            return type;
        }
    }

    private final List<TokenRule> tokenRules = new LinkedList<TokenRule>();
    private final T eofToken;

    public LexerGrammar(T eofToken) {
        this.eofToken = eofToken;
    }

    void add(String regex, T tokenType) {
        tokenRules.add(new TokenRule(Pattern.compile("^\\s*(" + regex + ")\\s*"), tokenType));
    }

    List<TokenRule> getRules() {
        return tokenRules;
    }

    T getEofToken() {
        return eofToken;
    }
}
