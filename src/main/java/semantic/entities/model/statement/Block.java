package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class Block extends Statement {
    private List<Statement> statements = new ArrayList<>();

    public Block(Token identifier) {
        super(identifier);
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
        statement.setParent(this);
    }

    @Override
    public void check() {

    }
}
