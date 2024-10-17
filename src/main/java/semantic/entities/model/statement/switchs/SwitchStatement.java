package main.java.semantic.entities.model.statement.switchs;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Statement;

abstract public class SwitchStatement {
    private Token identifier;
    private final Statement statement;

    public SwitchStatement(Token identifier, Statement statement) {
        this.statement = statement;
    }

    abstract public void check() throws SemanticException;
}
