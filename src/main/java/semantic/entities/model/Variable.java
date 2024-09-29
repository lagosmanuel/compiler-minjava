package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.exeptions.SemanticException;

public abstract class Variable extends Entity {
    protected Type type;

    public Variable(String var_name, Token var_token, Type var_type) {
        super(var_name, var_token);
        type = var_type;
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        type.validate();
    }
}
