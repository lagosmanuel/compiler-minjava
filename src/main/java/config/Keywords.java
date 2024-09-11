package main.java.config;

import main.java.model.TokenType;

import java.util.Map;

public class Keywords {
    public static final Map<String, TokenType> keywords = Map.ofEntries(
        Map.entry("class", TokenType.kwClass),
        Map.entry("boolean", TokenType.kwBoolean),
        Map.entry("if", TokenType.kwIf),
        Map.entry("switch", TokenType.kwSwitch),
        Map.entry("this", TokenType.kwThis),
        Map.entry("extends", TokenType.kwExtends),
        Map.entry("char", TokenType.kwChar),
        Map.entry("else", TokenType.kwElse),
        Map.entry("case", TokenType.kwCase),
        Map.entry("default", TokenType.kwDefault),
        Map.entry("var", TokenType.kwVar),
        Map.entry("void", TokenType.kwVoid),
        Map.entry("new", TokenType.kwNew),
        Map.entry("public", TokenType.kwPublic),
        Map.entry("int", TokenType.kwInt),
        Map.entry("float", TokenType.kwFloat),
        Map.entry("while", TokenType.kwWhile),
        Map.entry("break", TokenType.kwBreak),
        Map.entry("static", TokenType.kwStatic),
        Map.entry("return", TokenType.kwReturn),
        Map.entry("true", TokenType.trueLiteral),
        Map.entry("false", TokenType.falseLiteral),
        Map.entry("null", TokenType.nullLiteral)
    );
}
