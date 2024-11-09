package main.java.semantic.entities;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Unit;
import main.java.codegen.Labeler;
import main.java.config.CodegenConfig;
import main.java.exeptions.SemanticException;

import java.util.Objects;

public class Method extends Unit {
    public Method(String name, Token token) {
        super(name, token);
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();

        if (!isPrivate() &&
            isStatic() &&
            Objects.equals(return_type.getName(), "void") &&
            Objects.equals(name, "main") &&
            !SymbolTable.hasMain()
        ) {
            SymbolTable.foundMain();
            setLabel(Labeler.getLabel(CodegenConfig.MAIN_LABEL));
        }
    }
}
