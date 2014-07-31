package com.github.dkharrat.nexusdata.predicate.parser;

import com.github.dkharrat.nexusdata.predicate.ComparisonPredicate;
import com.github.dkharrat.nexusdata.predicate.CompoundPredicate;
import com.github.dkharrat.nexusdata.predicate.Expression;
import com.github.dkharrat.nexusdata.predicate.Predicate;

public class PredicateParser {

    public static enum TokenType {
        AND,
        OR,
        OPEN_PAREN,
        CLOSE_PAREN,
        EQUAL,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN_OR_EQUAL,
        NOT_EQUAL,
        GREATER_THAN,
        LESS_THAN,
        FIELD_NAME,
        CONSTANT,
        EOF
    }

    public static interface Precedence {
        public static final int OR          = 1;
        public static final int AND         = 2;
        public static final int EQUALITY    = 3;
        public static final int INEQUALITY  = 4;
        public static final int NOT         = 5;
        public static final int PREFIX      = 6;
        public static final int POSTFIX     = 7;
    }

    private final Parser<TokenType,Expression<?>> parser;
    private final static LexerGrammar<TokenType> lexerGrammar = new LexerGrammar<TokenType>(TokenType.EOF);
    static {
        lexerGrammar.add("\\(",     TokenType.OPEN_PAREN);
        lexerGrammar.add("\\)",     TokenType.CLOSE_PAREN);
        lexerGrammar.add("&&",      TokenType.AND);
        lexerGrammar.add("\\|\\|",  TokenType.OR);
        lexerGrammar.add("==",      TokenType.EQUAL);
        lexerGrammar.add("!=",      TokenType.NOT_EQUAL);
        lexerGrammar.add(">=",      TokenType.GREATER_THAN_OR_EQUAL);
        lexerGrammar.add("<=",      TokenType.LESS_THAN_OR_EQUAL);
        lexerGrammar.add(">",       TokenType.GREATER_THAN);
        lexerGrammar.add("<",       TokenType.LESS_THAN);
        lexerGrammar.add("(\"[^\"\\\\\\r\\n]*(?:\\\\.[^\"\\\\\\r\\n]*)*\")|\\d+|true|false|null|NULL",       TokenType.CONSTANT);
        lexerGrammar.add("[a-zA-Z][a-zA-Z0-9_]*",   TokenType.FIELD_NAME);
    }

    public PredicateParser(String text) {
        Lexer<TokenType> tokenizer = new Lexer<TokenType>(lexerGrammar, text);
        parser = new Parser<TokenType,Expression<?>>(tokenizer);
        parser.registerParslets(TokenType.OPEN_PAREN, new GroupParselet());
        parser.registerParslets(TokenType.EQUAL, new ComparisonParselet(ComparisonPredicate.Operator.EQUAL, Precedence.EQUALITY));
        parser.registerParslets(TokenType.NOT_EQUAL, new ComparisonParselet(ComparisonPredicate.Operator.NOT_EQUAL, Precedence.EQUALITY));
        parser.registerParslets(TokenType.GREATER_THAN, new ComparisonParselet(ComparisonPredicate.Operator.GREATER_THAN, Precedence.INEQUALITY));
        parser.registerParslets(TokenType.GREATER_THAN_OR_EQUAL, new ComparisonParselet(ComparisonPredicate.Operator.GREATER_THAN_OR_EQUAL, Precedence.INEQUALITY));
        parser.registerParslets(TokenType.LESS_THAN, new ComparisonParselet(ComparisonPredicate.Operator.LESS_THAN, Precedence.INEQUALITY));
        parser.registerParslets(TokenType.LESS_THAN_OR_EQUAL, new ComparisonParselet(ComparisonPredicate.Operator.LESS_THAN_OR_EQUAL, Precedence.INEQUALITY));
        parser.registerParslets(TokenType.AND, new LogicalParselet(CompoundPredicate.Operator.AND, Precedence.AND));
        parser.registerParslets(TokenType.OR, new LogicalParselet(CompoundPredicate.Operator.OR, Precedence.OR));
        parser.registerParslets(TokenType.CONSTANT, new ConstantParselet());
        parser.registerParslets(TokenType.FIELD_NAME, new NameParselet());
    }

    public Predicate parse() {
        return (Predicate)parser.parse();
    }
}

