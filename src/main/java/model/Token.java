package main.java.model;

import main.java.messages.TokenMessages;

public class Token {
    private final TokenType type;
    private final String lexeme;
    private final int line;
    private final int column;
    // private String lineText; TODO

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String toString() {
        return String.format(TokenMessages.FORMAT, type, lexeme, line);
    }
}
