package main.java.parser;

import main.java.model.TokenType;

import java.util.Collection;
import java.util.Set;

public class Recovery {
    public static final Collection<TokenType> synchronize_set = Set.of(
        TokenType.semicolon,
        TokenType.comma,
        TokenType.leftBrace,
        TokenType.kwFor,
        TokenType.kwWhile,
        TokenType.kwIf,
        TokenType.kwReturn,
        TokenType.kwBreak,
        TokenType.kwVar,
        TokenType.kwSwitch,
        TokenType.kwPublic,
        TokenType.kwPrivate,
        TokenType.kwStatic,
        TokenType.kwClass
    );
}
