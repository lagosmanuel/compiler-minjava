package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.semantic.entities.model.type.PrimitiveType;

import java.util.Objects;

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
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        Type returnType = expression != null? expression.checkType():null;
        if (returnType != null && !Objects.equals(returnType.getName(), SymbolTable.actualUnit.getReturn().getName()))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.RETURN_TYPE_MISMATCH,
                    SymbolTable.actualUnit.getReturn().getName(),
                    returnType.getName()
                ),
                getIdentifier()
            );
        else if (returnType == null && !Objects.equals(SymbolTable.actualUnit.getReturn().getName(), PrimitiveType.VOID))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.RETURN_NULL,
                    SymbolTable.actualUnit.getReturn().getName()
                ),
                getIdentifier()
            );
    }
}
