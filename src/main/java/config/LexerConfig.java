package main.java.config;

import main.java.model.TokenType;
import java.util.Map;

public class LexerConfig {
    public static final boolean CONTINUE_ON_ERROR = true;
    public static final boolean ENABLE_OPTIMIZATION = true;
    public static final char END_OF_FILE = Character.MAX_VALUE;
    public static final char NEWLINE = '\n';
    public static final char CARRY_RETURN = '\r';
    public static final int MAX_INT_LENGTH = 9;
    public static final Map<String, TokenType> RESERVED_WORDS = Keywords.getKeyWords();
}
