package main.java.symboltable.entities;

import main.java.model.Token;
import main.java.exeptions.SemanticException;
import main.java.symboltable.entities.type.Type;

public abstract class Variable extends Entity {
    protected final Type type;
    protected int offset;

    public Variable(String var_name, Token var_token, Type var_type) {
        super(var_name, var_token);
        type = var_type;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();
        type.validate();
    }
}
