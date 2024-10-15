package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;

public class Return extends Statement {
    private final Expression expression;

    public Return(Token identifier, Expression expression) {
        super(identifier);
        this.expression = expression;
    }

    public Return(Token identifier) {
        super(identifier);
        this.expression = null;
    }

    @Override
    public void check() {

    }
}
