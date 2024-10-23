package main.java.semantic.entities.model.statement.switchs;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Literal;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

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
        if (expressionType == null) return;
        Type literalType = literal != null? literal.checkType():null;
        if (literalType != null && !expressionType.compatible(literalType)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CASE_EXPRESSION_TYPE_NOT_COMPATIBLE,
                    expressionType.getName(),
                    literalType.getName()
                ),
                getIdentifier()
            );
        }
    }
}
