package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.CompositeExpression;
import main.java.semantic.entities.model.statement.expression.Expression;

public class For extends Statement {
    private final Statement assignment;
    private final CompositeExpression condition;
    private final Expression increment;
    private final Statement body;

    public For(Token identifier, Statement assignment, CompositeExpression condition, Expression increment, Statement body) {
        super(identifier);
        this.assignment = assignment;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    @Override
    public void check() {

    }
}
