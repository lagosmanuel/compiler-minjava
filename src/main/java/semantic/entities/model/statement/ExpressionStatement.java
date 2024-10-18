package main.java.semantic.entities.model.statement;

import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.exeptions.SemanticException;

public class ExpressionStatement extends Statement {
    private final Expression expression;

    public ExpressionStatement(Expression expression) {
        super(expression != null? expression.getIdentifier():null);
        this.expression = expression;
    }

    @Override
    public void check() throws SemanticException {

    }
}
