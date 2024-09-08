package main.java.parser;

import main.java.model.Error;
import main.java.model.Pair;
import main.java.exeptions.SyntacticException;

import java.util.List;
import java.util.Map;

public interface Parser {
    void parse() throws SyntacticException;
    Map<Integer, Pair<List<Error>, String>> getErrors();
}
