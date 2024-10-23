package main.java.semantic.entities.model;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.statement.Block;

public abstract class Statement {
    private Block parent;
    private final Token identifier;
    private boolean checked = false;
    private boolean breakable = false;
    private boolean returnable = false;

    public Statement(Token identifier) {
        this.identifier = identifier;
    }

    public Block getParent() {
        return parent;
    }

    public void setParent(Block parent) {
        this.parent = parent;
    }

    public Token getIdentifier() {
        return identifier;
    }

    public boolean checked() {
        return checked;
    }

    public void check() throws SemanticException {
       this.checked = true;
    }

    public void setBreakable() {
        this.breakable = true;
    }

    public boolean isBreakable() {
        return breakable || (parent != null && parent.isBreakable());
    }

    public boolean hasReturn() {
        return returnable;
    }

    public void setReturnable() {
        returnable = true;
    }

    public void unsetReturnable() {
        returnable = false;
    }
}
