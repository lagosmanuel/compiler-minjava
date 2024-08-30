package main.java.lexer;

import main.java.model.Pair;
import main.java.model.Error;
import main.java.model.Token;
import main.java.exeptions.LexicalException;

import java.util.List;
import java.util.Map;

public interface Lexer {
    Token nextToken() throws LexicalException;
    Map<Integer, Pair<List<Error>, String>> getErrors();
}
