package main.java.parser;

import main.java.model.TokenType;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lookup {

    public static final Collection<TokenType> Type = Set.of(
        TokenType.idClass,
        TokenType.kwBoolean,
        TokenType.kwChar,
        TokenType.kwInt,
        TokenType.kwFloat
    );

    public static final Collection<TokenType> MemberType = Stream.concat(
        Stream.of(TokenType.kwVoid),
        Lookup.Type.stream()
    ).collect(Collectors.toSet());

    public static final Collection<TokenType> PrimitiveLiteral = Set.of(
        TokenType.trueLiteral,
        TokenType.falseLiteral,
        TokenType.intLiteral,
        TokenType.floatLiteral,
        TokenType.charLiteral
    );

    public static final Collection<TokenType> ObjectLiteral = Set.of(
        TokenType.nullLiteral,
        TokenType.stringLiteral
    );

    public static final Collection<TokenType> Literal = Stream.concat(
        PrimitiveLiteral.stream(),
        ObjectLiteral.stream()
    ).collect(Collectors.toSet());

    public static final Collection<TokenType> Access = Set.of(
        TokenType.kwThis,
        TokenType.idMetVar,
        TokenType.kwNew,
        TokenType.idClass,
        TokenType.leftParenthesis
    );

    public static final Collection<TokenType> Operand = Stream.concat(
        Literal.stream(),
        Access.stream()
    ).collect(Collectors.toSet());

    public static final Collection<TokenType> BinaryOp = Set.of(
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

    public static final Collection<TokenType> Member = Stream.concat(
        Stream.of(
            TokenType.kwPublic,
            TokenType.kwPrivate,
            TokenType.kwStatic,
            TokenType.kwAbstract
        ),
        MemberType.stream()
    ).collect(Collectors.toSet());

    public static final Collection<TokenType> Expression = Stream.concat(
        Stream.of(
            TokenType.opPlus,
            TokenType.opMinus,
            TokenType.opNot
        ),
        Operand.stream()
    ).collect(Collectors.toSet());

    public static final Collection<TokenType> Statement = Stream.concat(Stream.concat(
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
        Expression.stream()),
        Type.stream()
    ).collect(Collectors.toSet());
}