package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class Block extends Statement {
    private final List<Statement> statements = new ArrayList<>();
    private final List<LocalVar> localVars = new ArrayList<>();

    public Block(Token identifier) {
        super(identifier);
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
        statement.setParent(this);
    }

    public boolean hasLocalVar(String name) {
        return localVars.stream().anyMatch(localVar -> localVar.hasLocalVar(name));
    }

    public LocalVar getLocalVar(String name) {
        return localVars.stream().filter(localVar -> localVar.hasLocalVar(name)).findFirst().get().getLocalVar(name);
    }

    public void addLocalVar(LocalVar localVar) {
        localVars.add(localVar);
    }

    @Override
    public void check() throws SemanticException {
        for (Statement statement:statements) statement.check();
    }

    @Override
    public void setBreakable() {
        for (Statement statement:statements) statement.setBreakable();
    }
}
