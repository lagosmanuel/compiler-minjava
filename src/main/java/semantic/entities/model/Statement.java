package main.java.semantic.entities.model;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.statement.Block;

public abstract class Statement {
    private Block parent;
    private final Token identifier;

    public Statement(Token identifier) {
        this.identifier = identifier;
    }

    public Block getParent() {
        return parent;
    }

    public void setParent(Block parent) {
        this.parent = parent;
    }

    public abstract void check() throws SemanticException;
}
