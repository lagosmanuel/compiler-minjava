package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;

public class While extends Statement {
    private final Expression condition;
    private final Statement body;

    public While(Token identifier, Expression condition, Statement body) {
        super(identifier);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void check() {

    }
}
