package main.java.semantic.entities.model.statement.chained;

import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.exeptions.SemanticException;

abstract public class Chained {
    private final Token identifier;
    private final Chained chained;
    private Token assignOp;

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

    public boolean isLeftValue() {
        return assignOp != null;
    }

    public Token getAssignOp() {
        return assignOp;
    }

    public void setAssignOp(Token assignOp) {
        this.assignOp = assignOp;
        if (chained != null) chained.setAssignOp(assignOp);
    }

    public abstract boolean isAssignable();

    public abstract boolean isStatement();

    public abstract Type checkType(Type type) throws SemanticException;

    public abstract void generate();
    public abstract void generate(String supername);

    public abstract boolean isVoid();
}
