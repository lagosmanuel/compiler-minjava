package main.java.semantic.entities.model.type;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;

import java.util.List;

public class ClassType extends Type {
    public ClassType(String class_name, Token class_token, List<Token> type_params_tokens) {
        super(class_name, class_token, type_params_tokens);
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        if (!SymbolTable.hasClass(getName()) && !SymbolTable.actualClass.hasGenericType(getName()))
            SymbolTable.throwException(SemanticErrorMessages.TYPE_NOTFOUND, getToken());
    }
}
