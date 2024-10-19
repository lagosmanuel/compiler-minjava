package main.java.semantic.entities.model.statement.expression;

import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.exeptions.SemanticException;

abstract public class Expression {
    private final Token identifier;
    private Type type = null;

    public Expression(Token identifier) {
        this.identifier = identifier;
    }

    public Token getIdentifier() {
        return identifier;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public abstract Type checkType() throws SemanticException;
}
