package main.java.exeptions;

import main.java.exeptions.CompilerException;

public class SemanticException extends CompilerException {
    public SemanticException(String message) {
        super(message);
    }
}
