package main.java.semantic.entities.model.statement.switchs;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;

public class Default extends SwitchStatement {
    public Default(Token identifier, Statement statement) {
        super(identifier, statement);
    }
}