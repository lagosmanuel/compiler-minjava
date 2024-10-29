package main.java.semantic.entities.model.statement.switchs;

import main.java.model.Token;
import main.java.semantic.entities.model.statement.Switch;
import main.java.semantic.entities.model.Statement;
import main.java.exeptions.SemanticException;

abstract public class SwitchStatement {
    private final Token identifier;
    private final Statement statement;
    private boolean checked = false;

    public SwitchStatement(Token identifier, Statement statement) {
        this.identifier = identifier;
        this.statement = statement;
        if (statement != null) statement.setBreakable();
    }

    public Token getIdentifier() {
        return identifier;
    }

    public boolean checked() {
        return checked;
    }

    public Statement getStatement() {
        return statement;
    }

    public boolean hasReturn() {
        return statement == null || statement.hasReturn();
    }

    public void check(Switch myswitch) throws SemanticException {
        checked = true;
        if (statement != null) statement.check();
    }
}
