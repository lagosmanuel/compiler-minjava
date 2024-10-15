package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;

public class Break extends Statement {
    public Break(Token identifier) {
        super(identifier);
    }

    @Override
    public void check() {

    }
}
