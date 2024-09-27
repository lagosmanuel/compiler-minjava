package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.exeptions.SemanticException;

public abstract class Variable extends Entity {
    protected Type type;

    public Variable(String var_name, Token var_token, Token type_token) {
        super(var_name, var_token);
        this.type = Type.createType(type_token);
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        type.validate();
    }
}
