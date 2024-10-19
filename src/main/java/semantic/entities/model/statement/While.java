package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.Expression;

public class While extends Statement {
    private final Expression condition;
    private final Statement body;

    public While(Token identifier, Expression condition, Statement body) {
        super(identifier);
        this.condition = condition;
        this.body = body;
        if (body != null) body.setBreakable();
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        Type conditionType = condition != null? condition.checkType():null;
        if (conditionType == null || !conditionType.isBoolean())
            SymbolTable.throwException(SemanticErrorMessages.WHILE_CONDITION_NOT_BOOLEAN, getIdentifier());
        body.check();
    }
}
