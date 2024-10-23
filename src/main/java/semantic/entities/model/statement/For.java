package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.CompositeExpression;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class For extends Statement {
    private final Statement assignment;
    private final CompositeExpression condition;
    private final Expression increment;
    private final Statement statement;
    private final Block body;

    public For(Token identifier, Statement assignment, CompositeExpression condition, Expression increment, Statement statement) {
        super(identifier);
        this.assignment = assignment;
        this.condition = condition;
        this.increment = increment;
        this.statement = statement;
        if (statement != null) statement.setBreakable();
        this.body = new Block(identifier);
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();

        body.setParent(getParent());
        body.check();
        this.setParent(body);
        SymbolTable.actualBlock = body;

        if (assignment != null) {
            assignment.setParent(body);
            assignment.check();
        }

        Type conditionType = condition != null? condition.checkType():null;
        Type incrementType = increment != null? increment.checkType():null;

        if (conditionType != null && !conditionType.isBoolean()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.FOR_CONDITION_NOT_BOOLEAN,
                    conditionType.getName()
                ),
                getIdentifier()
            );
        }

        if (increment != null && !(increment instanceof Assignment)) {
            SymbolTable.throwException(
                SemanticErrorMessages.FOR_INCREMENT_NOT_ASSIGNMENT,
                getIdentifier()
            );
        }

        if (statement != null) {
            statement.setParent(body);
            statement.check();
            if (statement.hasReturn()) setReturnable();
        }

        this.setParent(body.getParent());
        SymbolTable.actualBlock = body.getParent();
    }
}
