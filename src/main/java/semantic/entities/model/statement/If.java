package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;

public class If extends Statement {
    private final Expression condition;
    private final Statement then;

    public If(Token identifier, Expression condition, Statement then) {
        super(identifier);
        this.condition = condition;
        this.then = then;
    }

    @Override
    public void check() {

    }
}
