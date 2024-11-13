package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;

public class Nop extends Statement {
    public Nop(Token identifier) {
        super(identifier);
    }

    @Override
    public void generate() {
        // Do nothing
    }
}
