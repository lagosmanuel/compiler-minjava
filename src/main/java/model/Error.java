package main.java.model;

public class Error {
    private final String message;
    private final String lexeme;
    private final int line;
    private final int column;

    public Error(String message, String lexeme, int line, int column) {
        this.message = message;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
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
}
