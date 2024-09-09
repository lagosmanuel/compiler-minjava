package main.java.lexer;

import main.java.model.Token;
import main.java.exeptions.LexicalException;

public interface Lexer {
    Token nextToken() throws LexicalException;
}