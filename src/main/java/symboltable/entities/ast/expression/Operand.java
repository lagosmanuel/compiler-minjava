package main.java.symboltable.entities.ast.expression;

import main.java.model.Token;

abstract public class Operand extends BasicExpression {
    public Operand(Token identifier) {
        super(identifier);
    }
}
