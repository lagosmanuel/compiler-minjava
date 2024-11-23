package main.java.symboltable.entities.ast.expression;

import main.java.model.Token;

public abstract class BasicExpression extends CompositeExpression {
    public BasicExpression(Token identifier) {
        super(identifier);
    }
}
