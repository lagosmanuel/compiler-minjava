package main.java.symboltable.entities.ast.expression;

import main.java.model.Token;

abstract public class CompositeExpression extends Expression {
    public CompositeExpression(Token identifier) {
        super(identifier);
    }
}
