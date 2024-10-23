package main.java.semantic.entities.model.statement;

import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.semantic.entities.model.statement.switchs.SwitchStatement;
import main.java.exeptions.SemanticException;

import java.util.List;

public class Switch extends Statement {
    private final Expression expression;
    private final List<SwitchStatement> statements;

    public Switch(Token identifier, Expression expression, List<SwitchStatement> statements) {
        super(identifier);
        this.expression = expression;
        this.statements = statements;
        this.setReturnable();
    }

    @Override
    public void check() throws SemanticException {
        Type expressionType = expression != null? expression.checkType():null;
        if (expressionType == null) return;
        if (expressionType.isNull()) {
            SymbolTable.throwException(
                SemanticErrorMessages.SWITCH_EXPRESSION_TYPE_NULL,
                getIdentifier()
            );
        } else for (SwitchStatement statement:statements) {
            statement.check(expressionType);
            if (!statement.hasReturn()) unsetReturnable();
        }
    }
}
