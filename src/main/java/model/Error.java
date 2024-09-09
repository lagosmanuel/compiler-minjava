package main.java.model;

public class Error {
    private final String message;
    private final String lexeme;
    private final int line;
    private final int column;
    private final ErrorType type;

    public Error(String message, String lexeme, int line, int column, ErrorType type) {
        this.message = message;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
        this.type = type;
    }

    public String getMessage() {
        return message;
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

    public ErrorType getType() {
        return type;
    }
}
