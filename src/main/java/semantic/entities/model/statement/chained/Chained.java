package main.java.semantic.entities.model.statement.chained;

import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.exeptions.SemanticException;

abstract public class Chained {
    private final Token identifier;
    private final Chained chained;

    public Chained(Token identifier, Chained chained) {
        this.identifier = identifier;
        this.chained = chained;
    }

    public Token getIdentifier() {
        return identifier;
    }

    public Chained getChained() {
        return chained;
    }

    public abstract boolean isAssignable();

    public abstract boolean isStatement();

    public abstract Type checkType(Type type) throws SemanticException;

    public void generate() {}
}
