package main.java.semantic.entities.model.statement.expression;

import main.java.model.Token;

abstract public class Operand extends BasicExpression {
    public Operand(Token identifier) {
        super(identifier);
    }
}
