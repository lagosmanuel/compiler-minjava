package main.java.semantic.entities.model.statement.expression;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
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
        Type operandType = operand.checkType();

        return switch (operator.getType()) {
            case opPlus, opMinus -> {
                if (operandType != null && operandType.isNumeric()) {
                    yield operandType;
                } else {
                    SymbolTable.throwException(
                        String.format(
                            SemanticErrorMessages.PLUS_MINUS_OPERAND_NOT_NUMERIC,
                            operator.getLexeme(),
                            operandType != null? operandType.getName():"null"
                        ),
                        getIdentifier()
                    );
                    yield null;
                }
            }
            case opNot -> {
                if (operandType != null && operandType.isBoolean()) {
                    yield operandType;
                } else {
                    SymbolTable.throwException(
                        String.format(
                            SemanticErrorMessages.NOT_CONDITION_NOT_BOOLEAN,
                            operator.getLexeme(),
                            operandType != null? operandType.getName():"null"
                        ),
                        getIdentifier()
                    );
                    yield null;
                }
            }
            default -> {
                SymbolTable.throwException(
                    String.format(
                        SemanticErrorMessages.UNARYOP_INVALID,
                        operator.getLexeme()
                    ),
                    getIdentifier()
                );
                yield null;
            }
        };
    }
}
