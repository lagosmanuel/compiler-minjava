package main.java.semantic.entities.model.type;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;

public class VarType extends Type {
    public VarType(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();
    }
}