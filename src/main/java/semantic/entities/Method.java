package main.java.semantic.entities;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Unit;

import java.util.Objects;

public class Method extends Unit {
    public Method(String name, Token token) {
        super(name, token);
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        if (is_static && Objects.equals(name, "main") && Objects.equals(return_type.getName(), "void")) //TODO: check
            SymbolTable.foundMain();
    }
}
