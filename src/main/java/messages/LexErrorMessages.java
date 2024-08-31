package main.java.messages;

public class LexErrorMessages {
    public static final String FORMAT = "Lexical error at line %d, column %d: <%s> %s\n";
    public static final String ILLEGAL_CHAR = "Illegal character";
    public static final String LITERAL_CHAR_EMPTY = "Empty character literal";
    public static final String LITERAL_CHAR_NOT_CLOSED = "Unclosed character literal";
    public static final String LITERAL_CHAR_BAD_ESCAPED = "Illegal escape character in character literal";
    public static final String LITERAL_STR_NOT_CLOSED = "Unclosed string literal";
    public static final String LITERAL_STR_BAD_ESCAPED = "Illegal escape character in string literal";
    public static final String LITERAL_FLOAT_EXP_INVALID = "Malformed floating point literal's exponent";
    public static final String LITERAL_FLOAT_EXP_SIGN_INVALID = "Malformed floating point literal's exponent sign";
    public static final String LITERAL_FLOAT_TOO_LARGE = "Float literal is too large";
    public static final String LITERAL_INT_TOO_LARGE = "Integer literal is too large";
    public static final String OP_AND_INVALID = "Invalid AND operator";
    public static final String OP_OR_INVALID = "Invalid OR operator";
    public static final String COMMENT_BLOCK_NOT_CLOSED = "Unclosed block comment";
}