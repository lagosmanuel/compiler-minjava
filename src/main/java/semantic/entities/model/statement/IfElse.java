package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;

public class IfElse extends If {
    private final Statement elseStatement;

    public IfElse(Token identifier, Expression condition, Statement then, Statement elseStatement) {
        super(identifier, condition, then);
        this.elseStatement = elseStatement;
    }

    @Override
    public void check() {

    }
}
