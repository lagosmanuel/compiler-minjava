package main.java.semantic.entities.model.statement.expression;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;

public class UnaryExpression extends BasicExpression {
    private final Operand operand;
    private final Token operator;

    public UnaryExpression(Operand operand, Token operator) {
        super(operator);
        this.operand = operand;
        this.operator = operator;
    }

    @Override
    public Type checkType() throws SemanticException {
        return null;
    }
}
