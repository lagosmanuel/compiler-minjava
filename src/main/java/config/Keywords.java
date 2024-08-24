package main.java.config;

import main.java.model.TokenType;

import java.util.HashMap;
import java.util.Map;

public class Keywords {
    static Map<String, TokenType> getKeyWords() {
        Map<String, TokenType> keywords = new HashMap<>();
        keywords.put("class", TokenType.kwClass);
        keywords.put("boolean", TokenType.kwBoolean);
        keywords.put("if", TokenType.kwIf);
        keywords.put("switch", TokenType.kwSwitch);
        keywords.put("this", TokenType.kwThis);
        keywords.put("extends", TokenType.kwExtends);
        keywords.put("char", TokenType.kwChar);
        keywords.put("else", TokenType.kwElse);
        keywords.put("case", TokenType.kwCase);
        keywords.put("var", TokenType.kwVar);
        keywords.put("void", TokenType.kwVoid);
        keywords.put("new", TokenType.kwNew);
        keywords.put("public", TokenType.kwPublic);
        keywords.put("int", TokenType.kwInt);
        keywords.put("float", TokenType.kwFloat);
        keywords.put("while", TokenType.kwWhile);
        keywords.put("break", TokenType.kwBreak);
        keywords.put("static", TokenType.kwStatic);
        keywords.put("return", TokenType.kwReturn);
        keywords.put("true", TokenType.trueLiteral);
        keywords.put("false", TokenType.falseLiteral);
        keywords.put("null", TokenType.nullLiteral);
        return keywords;
    }
}
