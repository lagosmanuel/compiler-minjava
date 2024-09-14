package main.java.parser;

import main.java.model.TokenType;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Follow {
    public static final Collection<TokenType> Statement =
        Stream.concat(
            Stream.of(
                TokenType.rightBrace,
                TokenType.kwElse,
                TokenType.kwCase,
                TokenType.kwDefault
            ),
            Lookup.Statement.stream()
        ).collect(Collectors.toSet());

    public static final Collection<TokenType> Expression =
        Set.of(
            TokenType.comma,
            TokenType.rightParenthesis,
            TokenType.semicolon
        );

    public static final Collection<TokenType> CompositeExpression =
        Stream.concat(
            Stream.of(
                TokenType.opAssign,
                TokenType.opPlusAssign,
                TokenType.opMinusAssign,
                TokenType.semicolon
            ),
            Follow.Expression.stream()
        ).collect(Collectors.toSet());

    public static final Collection<TokenType> BasicExpression =
        Stream.concat(
            Follow.CompositeExpression.stream(),
            Lookup.BinaryOp.stream()
        ).collect(Collectors.toSet());

    public static final Collection<TokenType> Primary =
        Stream.concat(
            Stream.of(
                TokenType.dot
            ),
            Follow.BasicExpression.stream()
        ).collect(Collectors.toSet());
}