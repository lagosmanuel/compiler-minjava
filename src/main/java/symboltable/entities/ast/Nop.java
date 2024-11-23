package main.java.symboltable.entities.ast;

import main.java.model.Token;

public class Nop extends Statement {
    public Nop(Token identifier) {
        super(identifier);
    }

    @Override
    public void generate() {
        // Do nothing
    }
}
