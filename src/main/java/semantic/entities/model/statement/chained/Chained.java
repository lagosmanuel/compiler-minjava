package main.java.semantic.entities.model.statement.chained;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;

abstract public class Chained {
    private final Chained chained;
    private final Token identifier;

    public Chained(Token identifier, Chained chained) {
        this.identifier = identifier;
        this.chained = chained;
    }

    public abstract Type checkType() throws SemanticException;
}
