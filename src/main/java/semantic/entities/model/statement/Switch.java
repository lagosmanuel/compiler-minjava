package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;

public class Switch extends Statement {
    private final Expression expression;
    //private final Instruction body; TODO:

    public Switch(Token identifier, Expression expression) {
        super(identifier);
        this.expression = expression;
    }

    @Override
    public void check() {

    }
}
