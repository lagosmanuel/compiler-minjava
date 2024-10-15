package main.java.semantic.entities.model.statement.expression;

import main.java.model.Token;

abstract public class CompositeExpression extends Expression {
    public CompositeExpression(Token identifier) {
        super(identifier);
    }
}
