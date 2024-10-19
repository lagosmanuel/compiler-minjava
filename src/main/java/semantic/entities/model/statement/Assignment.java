package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
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
        if (getType() != null) return getType();
        Type leftType = left.checkType();
        Type rightType = right.checkType();

        if (leftType == null || !leftType.compatible(rightType))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.TYPE_NOT_COMPATIBLE,
                    leftType != null? leftType.getName():"null",
                    rightType != null? rightType.getName():"null"
                ),
            getIdentifier()
        );

        if (operator.getType() != TokenType.opAssign && leftType != null && !leftType.isNumeric())
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.ASSIGN_INCREMENT_TYPE_NOT_NUMERIC,
                    leftType.getName()
                ),
                getIdentifier()
            );

        setType(leftType);
        return getType();
    }
}
