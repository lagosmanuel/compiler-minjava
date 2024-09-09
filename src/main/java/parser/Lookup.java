package main.java.parser;

import main.java.model.TokenType;

import java.util.List;
import java.util.stream.Stream;

public class Lookup {
    public static final List<TokenType> Expression = List.of(
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
        TokenType.idClass,
        TokenType.leftParenthesis
    );

    public static final List<TokenType> Statement = Stream.concat(
        Stream.of(
            TokenType.semicolon,
            TokenType.kwVar,
            TokenType.kwReturn,
            TokenType.kwBreak,
            TokenType.kwIf,
            TokenType.kwWhile,
            TokenType.kwSwitch,
            TokenType.leftBrace
        ),
        Expression.stream()
    ).toList();

    public static final List<TokenType> Access = List.of(
        TokenType.kwThis,
        TokenType.idMetVar,
        TokenType.kwNew,
        TokenType.idClass,
        TokenType.leftParenthesis
    );

    public static final List<TokenType> Type = List.of(
        TokenType.idClass,
        TokenType.kwBoolean,
        TokenType.kwChar,
        TokenType.kwInt
    );

    public static final List<TokenType> MemberType = Stream.concat(
        Stream.of(TokenType.kwVoid),
        Type.stream()
    ).toList();

    public static final List<TokenType> Constructor = List.of(
        TokenType.kwPublic
    );

    public static final List<TokenType> Member = Stream.concat(
        Constructor.stream(),
        MemberType.stream()
    ).toList();

    public static final List<TokenType> PrimitiveLiteral = List.of(
        TokenType.trueLiteral,
        TokenType.falseLiteral,
        TokenType.intLiteral,
        TokenType.charLiteral
    );

    public static final List<TokenType> ObjectLiteral = List.of(
        TokenType.nullLiteral,
        TokenType.stringLiteral
    );

    public static final List<TokenType> Literal = Stream.concat(
        PrimitiveLiteral.stream(),
        ObjectLiteral.stream()
    ).toList();

    public static final List<TokenType> Operand = Stream.concat(
        Literal.stream(),
        Access.stream()
    ).toList();

    public static final List<TokenType> BinaryOp = List.of(
        TokenType.opOr,
        TokenType.opAnd,
        TokenType.opEqual,
        TokenType.opNotEqual,
        TokenType.opLess,
        TokenType.opGreater,
        TokenType.opLessEqual,
        TokenType.opGreaterEqual,
        TokenType.opPlus,
        TokenType.opMinus,
        TokenType.opTimes,
        TokenType.opDiv,
        TokenType.opMod
    );
}