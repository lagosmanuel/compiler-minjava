package main.java.semantic.entities.model.statement.switchs;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;

abstract public class SwitchStatement {
    private final Statement statement;
    private boolean checked = false;

    public SwitchStatement(Token identifier, Statement statement) {
        if (statement != null) statement.setBreakable();
        this.statement = statement;
    }

    public boolean checked() {
        return checked;
    }

    public Token getIdentifier() {
        return statement != null? statement.getIdentifier():null;
    }

    public void check(Type expressionType) throws SemanticException {
        checked = true;
        statement.check();
    }
}
