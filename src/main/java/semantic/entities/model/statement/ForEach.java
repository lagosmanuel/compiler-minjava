package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.CompositeExpression;

public class ForEach extends Statement {
    private final LocalVar declaration;
    private final CompositeExpression iterable;
    private final Statement body;

    public ForEach(Token identifier, LocalVar declaration, CompositeExpression iterable, Statement body) {
        super(identifier);
        this.declaration = declaration;
        this.iterable = iterable;
        this.body = body;
    }

    @Override
    public void check() {

    }
}