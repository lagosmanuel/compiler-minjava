package main.java.semantic.entities.model.statement.expression;

import main.java.model.Token;
import main.java.semantic.entities.model.Type;

public class BinaryExpression extends CompositeExpression {
    private final CompositeExpression left;
    private final BasicExpression right;
    private final Token operator;

    public BinaryExpression(CompositeExpression left, BasicExpression right, Token operator) {
        super(operator);
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public CompositeExpression getLeft() {
        return left;
    }

    public BasicExpression getRight() {
        return right;
    }

    public Token getOperator() {
        return operator;
    }

    @Override
    public Type checkType() throws SecurityException {
        return null;
    }
}
