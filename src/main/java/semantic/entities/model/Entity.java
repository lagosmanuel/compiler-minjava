package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.exeptions.SemanticException;

public abstract class Entity {
    protected String name;
    protected final Token token;
    private boolean is_validated;

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
        return is_validated;
    }

    public void validate() throws SemanticException {
        is_validated = true;
    }
}
