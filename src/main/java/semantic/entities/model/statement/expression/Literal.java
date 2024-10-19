package main.java.semantic.entities.model.statement.expression;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;

public class Literal extends Operand {
    private final Token value;

    public Literal(Token value) {
        super(value);
        this.value = value;
    }

    @Override
    public Type checkType() throws SemanticException {
        return switch (value.getType()) {
            case intLiteral, floatLiteral, stringLiteral, charLiteral, trueLiteral, falseLiteral, nullLiteral ->
                Type.createType(value.getType());
            default -> {
                SymbolTable.throwException(SemanticErrorMessages.LITERAL_INVALID_TYPE, value); yield null;
            }
        };
    }
}
