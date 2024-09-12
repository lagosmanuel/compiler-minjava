package main.java.parser;

import main.java.exeptions.SyntacticException;

public interface Parser {
    void parse() throws SyntacticException;
}
