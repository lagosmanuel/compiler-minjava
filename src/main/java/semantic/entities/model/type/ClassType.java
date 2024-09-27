package main.java.semantic.entities.model.type;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;

public class ClassType extends Type {
    public ClassType(String class_name, Token class_token) {
        super(class_name, class_token);
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        if (!SymbolTable.hasClass(getName()) && !SymbolTable.actualClass.hasGenericType(getName()))
            SymbolTable.throwException(SemanticErrorMessages.TYPE_NOTFOUND, getToken());
    }
}
