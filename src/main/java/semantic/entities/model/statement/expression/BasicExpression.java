package main.java.semantic.entities.model.statement.expression;

import main.java.model.Token;

public abstract class BasicExpression extends CompositeExpression {
    public BasicExpression(Token identifier) {
        super(identifier);
    }
}
