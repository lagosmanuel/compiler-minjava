package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.semantic.entities.model.statement.switchs.SwitchStatement;

import java.util.List;

public class Switch extends Statement {
    private final Expression expression;
    private final List<SwitchStatement> statements;

    public Switch(Token identifier, Expression expression, List<SwitchStatement> statements) {
        super(identifier);
        this.expression = expression;
        this.statements = statements;
    }

    @Override
    public void check() {

    }
}
