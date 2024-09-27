package main.java.semantic.entities;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Unit;

import java.util.Objects;

public class Constructor extends Unit {
    public Constructor(String cons_name, Token cons_token) {
        super(cons_name, cons_token);
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        if (!Objects.equals(getName(), SymbolTable.actualClass.getName()))
            SymbolTable.throwException(SemanticErrorMessages.CONSTRUCTOR_NAME_MISMATCH, getToken());
    }
}
