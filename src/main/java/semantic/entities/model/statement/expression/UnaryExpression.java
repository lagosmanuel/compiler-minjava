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
        if (operand == null || operator == null) return null;
        Type operandType = operand.checkType();

        return switch (operator.getType()) {
            case opPlus, opMinus -> {
                operandType = operandType != null? operandType.convertNumeric():null;
                if (operandType != null && !operandType.isNumeric()) {
                    SymbolTable.throwException(
                        String.format(
                            SemanticErrorMessages.PLUS_MINUS_OPERAND_NOT_NUMERIC,
                            operator.getLexeme(),
                            operandType.getName()
                        ),
                        getIdentifier()
                    );
                    yield null;
                } else yield operandType;
            }
            case opNot -> {
                if (operandType != null && !operandType.isBoolean()) {
                    SymbolTable.throwException(
                        String.format(
                            SemanticErrorMessages.NOT_CONDITION_NOT_BOOLEAN,
                            operator.getLexeme(),
                            operandType.getName()
                        ),
                        getIdentifier()
                    );
                    yield null;
                } else yield operandType;
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
