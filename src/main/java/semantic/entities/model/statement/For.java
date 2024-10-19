package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.CompositeExpression;
import main.java.semantic.entities.model.statement.expression.Expression;

public class For extends Statement {
    private final Statement assignment;
    private final CompositeExpression condition;
    private final Expression increment;
    private final Statement body;

    public For(Token identifier, Statement assignment, CompositeExpression condition, Expression increment, Statement body) {
        super(identifier);
        this.assignment = assignment;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
        if (body != null) body.setBreakable();
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        Type conditionType = condition.checkType();
        Type incrementType = increment.checkType();
        if (conditionType == null || !conditionType.isBoolean())
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.FOR_CONDITION_NOT_BOOLEAN,
                    conditionType != null? conditionType.getName():"null"
                ),
                getIdentifier()
            );
        if (!(increment instanceof Assignment))
            SymbolTable.throwException(
                SemanticErrorMessages.FOR_INCREMENT_NOT_ASSIGNMENT,
                getIdentifier()
            );
        if (assignment != null) assignment.check();
        if (body != null) body.check();
    }
}
