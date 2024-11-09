package main.java.semantic.entities.model.statement.expression;

import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.exeptions.SemanticException;

abstract public class Expression {
    private final Token identifier;
    private Type type = null;
    private Token assignOp;

    public Expression(Token identifier) {
        this.identifier = identifier;
    }

    public Token getIdentifier() {
        return identifier;
    }

    public boolean isLeftValue() {
        return assignOp != null;
    }

    public Token getAssignOp() {
        return assignOp;
    }

    public void setAssignOp(Token assignOp) {
        this.assignOp = assignOp;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isAssignable() {
        return false;
    }

    public boolean isStatement() {
        return false;
    }

    public abstract Type checkType() throws SemanticException;

    public abstract void generate();
}
