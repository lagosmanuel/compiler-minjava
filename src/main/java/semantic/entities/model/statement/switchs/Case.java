package main.java.semantic.entities.model.statement.switchs;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.Literal;

import java.util.Objects;

public class Case extends SwitchStatement {
    private final Literal literal;

    public Case(Token identifier, Literal literal, Statement statement) {
        super(identifier, statement);
        this.literal = literal;
    }

    @Override
    public void check(Type expressionType) throws SemanticException {
        if (checked()) return;
        super.check(expressionType);
        Type literalType = literal.checkType();
        if (literalType == null || !Objects.equals(literalType.getName(), expressionType.getName()))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CASE_EXPRESSION_TYPE_NOT_COMPATIBLE,
                    expressionType.getName(),
                    literalType != null? literalType.getName():"null"
                ),
                getIdentifier()
            );
    }
}
