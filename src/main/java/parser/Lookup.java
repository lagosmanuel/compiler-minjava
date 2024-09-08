package main.java.parser;

import main.java.model.TokenType;

import java.util.List;
import java.util.stream.Stream;

public class Lookup {
    public static List<TokenType> Expression = List.of(
            TokenType.opPlus,
            TokenType.opMinus,
            TokenType.opNot,
            TokenType.trueLiteral,
            TokenType.falseLiteral,
            TokenType.intLiteral,
            TokenType.charLiteral,
            TokenType.nullLiteral,
            TokenType.stringLiteral,
            TokenType.kwThis,
            TokenType.idMetVar,
            TokenType.kwNew,
            TokenType.idClassVar,
            TokenType.leftParenthesis
    );

    public static List<TokenType> Statement = Stream.concat(Stream.of(
            TokenType.semicolon,
            TokenType.kwVar,
            TokenType.kwReturn,
            TokenType.kwBreak,
            TokenType.kwIf,
            TokenType.kwWhile,
            TokenType.kwSwitch,
            TokenType.leftBrace
    ), Expression.stream()).toList();
}
