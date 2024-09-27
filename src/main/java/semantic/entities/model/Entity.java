package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.exeptions.SemanticException;

public abstract class Entity {
    protected String name;
    protected Token token;
    private boolean isValidated = false;

    public Entity(String name, Token token) {
        this.name = name;
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public Token getToken() {
        return token;
    }

    public void setName(String new_name) {
        this.name = new_name;
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void validate() throws SemanticException {
        if (isValidated) return;
        isValidated = true;
    }
}
