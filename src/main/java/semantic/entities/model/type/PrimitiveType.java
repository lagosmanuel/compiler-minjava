package main.java.semantic.entities.model.type;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;

import java.lang.String;
import java.util.Set;

public class PrimitiveType extends Type {
    public static final Set<String> types = Set.of(
        "int", "float", "string", "boolean", "char", "void"
    );

    public PrimitiveType(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        if (!types.contains(getName()))
            SymbolTable.throwException(SemanticErrorMessages.TYPE_NOTFOUND, getToken());
    }
}
