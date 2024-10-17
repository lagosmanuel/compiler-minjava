package main.java.semantic.entities.model.statement.switchs;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Literal;

public class Case extends SwitchStatement {
    private final Literal literal;

    public Case(Token identifier, Literal literal, Statement statement) {
        super(identifier, statement);
        this.literal = literal;
    }

    @Override
    public void check() throws SemanticException {

    }
}
