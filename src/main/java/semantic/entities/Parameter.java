package main.java.semantic.entities;

import main.java.model.Token;
import main.java.semantic.entities.model.Variable;

public class Parameter extends Variable {
    public Parameter(String name, Token token, Token type_token) {
        super(name, token, type_token);
    }
}
