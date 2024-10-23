package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class Block extends Statement {
    private final List<Statement> statements = new ArrayList<>();
    private final List<LocalVar> localVars = new ArrayList<>();

    public Block(Token identifier) {
        super(identifier);
    }

    public void addStatement(Statement statement) {
        if (statement == null) return;
        statements.add(statement);
        statement.setParent(this);
    }

    public boolean hasLocalVar(String varName) {
        return localVars.stream().anyMatch(
            localVar -> Objects.equals(localVar.getIdentifier().getLexeme(), varName));
    }

    public LocalVar getLocalVar(String varName) {
        return localVars.stream().filter(
            localVar -> Objects.equals(localVar.getIdentifier().getLexeme(), varName))
                .findFirst()
                .orElse(null);
    }

    public void addLocalVar(LocalVar localVar) {
        if (localVar == null) return;
        localVars.add(localVar);
    }

    public void removeLocalVar(LocalVar localVar) {
        if (localVar == null) return;
        localVars.remove(localVar);
    }

    public List<LocalVar> getLocalVars() {
        return localVars;
    }

    @Override
    public void check() throws SemanticException {
        SymbolTable.actualBlock = this;
        if (getParent() != null) getParent().getLocalVars().forEach(this::addLocalVar);
        for (Statement statement:statements) {
            if (hasReturn()) {
                SymbolTable.throwException(
                    SemanticErrorMessages.UNREACHABLE_CODE,
                    statement.getIdentifier()
                );
            }
            statement.check();
            if (statement.hasReturn()) setReturnable();
        }
        SymbolTable.actualBlock = getParent();
    }
}
