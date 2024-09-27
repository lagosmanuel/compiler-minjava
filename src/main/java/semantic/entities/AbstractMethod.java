package main.java.semantic.entities;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class AbstractMethod extends Method {
    public AbstractMethod(String method_name, Token method_token) {
        super(method_name, method_token);
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        if (!SymbolTable.actualClass.isAbstract())
            SymbolTable.throwException(SemanticErrorMessages.ABSTRACT_METHOD_IN_NON_ABSTRACT_CLASS, getToken());
        if (is_private)
            SymbolTable.throwException(SemanticErrorMessages.ABSTRACT_METHOD_PRIVATE, getToken());
        if (is_static)
            SymbolTable.throwException(SemanticErrorMessages.ABSTRACT_METHOD_STATIC, getToken());
    }
}
