package main.java.semantic.entities.model.statement.expression;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.type.PrimitiveType;

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
    public Type checkType() throws SemanticException {
        Type leftType = left.checkType();
        Type rightType = right.checkType();

        return switch (operator.getType()) {
            case opPlus, opMinus, opTimes, opDiv, opMod -> {
                leftType = leftType != null? leftType.convertNumeric():null;
                rightType = rightType != null? rightType.convertNumeric():null;

                if (leftType != null && rightType != null && leftType.isNumeric() && rightType.isNumeric()) {
                    yield leftType.isFloat()? leftType:rightType;
                } else {
                    SymbolTable.throwException(
                        String.format(
                            SemanticErrorMessages.BINARY_OPERAND_NOT_NUMERIC,
                            operator.getLexeme(),
                            leftType != null? leftType.getName():"null",
                            rightType != null? rightType.getName():"null"
                        ),
                        getIdentifier()
                    );
                    yield null;
                }
            }
            case opAnd, opOr -> {
                if (leftType != null && rightType != null && leftType.isBoolean() && rightType.isBoolean()) {
                    yield leftType;
                } else {
                    SymbolTable.throwException(
                        String.format(
                            SemanticErrorMessages.BINARY_OPERAND_NOT_BOOLEAN,
                            operator.getLexeme(),
                            leftType != null? leftType.getName():"null",
                            rightType != null? rightType.getName():"null"
                        ),
                        getIdentifier()
                    );
                    yield null;
                }
            }
            case opEqual, opNotEqual -> {
                if (leftType != null && rightType != null && leftType.compatible(rightType)) {
                    yield Type.createType(
                        new Token(TokenType.kwBoolean, PrimitiveType.BOOLEAN, 0, 0),
                        null
                    );
                } else {
                    SymbolTable.throwException(
                        String.format(
                            SemanticErrorMessages.BINARY_OPERAND_NOT_COMPATIBLE,
                            operator.getLexeme(),
                            leftType != null? leftType.getName():"null",
                            rightType != null? rightType.getName():"null"
                        ),
                        getIdentifier()
                    );
                    yield null;
                }
            }
            case opLess, opGreater, opLessEqual, opGreaterEqual -> {
                if (leftType!= null) leftType = leftType.convertNumeric();
                if (rightType != null) rightType = rightType.convertNumeric();

                if (leftType != null && rightType != null && leftType.isNumeric() && rightType.isNumeric()) {
                    yield Type.createType(
                            new Token(TokenType.kwBoolean, PrimitiveType.BOOLEAN, 0, 0),
                            null
                    );
                } else {
                    SymbolTable.throwException(
                        String.format(
                            SemanticErrorMessages.BINARY_OPERAND_NOT_NUMERIC,
                            operator.getLexeme(),
                            leftType != null? leftType.getName():"null",
                            rightType != null? rightType.getName():"null"
                        ),
                        getIdentifier()
                    );
                    yield null;
                }
            }
            default -> {
                SymbolTable.throwException(
                    String.format(
                        SemanticErrorMessages.BINARYOP_INVALID,
                        operator.getLexeme()
                    ),
                    getIdentifier()
                );
                yield null;
            }
        };
    }
}
