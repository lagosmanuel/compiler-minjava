package main.java.messages;

public class LexErrorMessages {
    public static final String FORMAT = "Lexical error at line %d, column %d: %s %s\n";
    public static final String UNEXPECTED_SYMBOL = "Unexpected symbol";
    public static final String LITERAL_CHAR_EMPTY = "Invalid empty character literal";
    public static final String LITERAL_CHAR_NOT_CLOSED = "Character literal not closed";
    public static final String LITERAL_STR_NOT_CLOSED = "String literal not closed";
    public static final String LITERAL_STR_BAD_ESCAPED = "Invalid escape sequence in string literal";
    public static final String LITERAL_FLOAT_INVALID = "Float literal is invalid";
    public static final String LITERAL_FLOAT_TOO_LARGE = "Float literal is too large";
    public static final String LITERAL_INT_TOO_LARGE = "Integer literal is too large";
    public static final String OP_AND_INVALID = "Invalid AND operator";
    public static final String OP_OR_INVALID = "Invalid OR operator";
    public static final String COMMENT_BLOCK_NOT_CLOSED = "Block comment not closed";
}