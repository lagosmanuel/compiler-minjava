package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.CompositeExpression;
import main.java.semantic.entities.model.statement.expression.Expression;

public class Assignment extends Expression {
    private final CompositeExpression left;
    private final CompositeExpression right;
    private final Token operator;

    public Assignment(CompositeExpression left, CompositeExpression right, Token operator) {
        super(operator);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public Type checkType() throws SemanticException {
        return null;
    }
}
