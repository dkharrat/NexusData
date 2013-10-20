package org.nexusdata.predicate.parser;

class Token<TokenType> {
    private final TokenType type;
    private final String text;

    Token(TokenType type, String value) {
        this.type = type;
        this.text = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
