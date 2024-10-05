package main.java.semantic.entities.model.type;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;

import java.util.List;

public class ClassType extends Type {
    public ClassType(String class_name, Token class_token, List<TypeVar> type_params) {
        super(class_name, class_token, type_params);
    }

    public ClassType(String class_name, Token class_token) {
        super(class_name, class_token);
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();

        if (!SymbolTable.hasClass(getName()) && !SymbolTable.actualClass.hasTypeParameter(getName()))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CLASS_NOT_DECLARED,
                    getName()
                ),
                getToken()
            );

        if (SymbolTable.actualClass.hasTypeParameter(getName())) {
            if (getTypeParamsCount() > 0)
                SymbolTable.throwException(SemanticErrorMessages.TYPE_PARAMETER_RECURSIVE, getToken());
        } else if (SymbolTable.hasClass(getName())) {
            if (SymbolTable.getClass(getName()).getTypeParametersCount() != getTypeParamsCount())
                SymbolTable.throwException(SemanticErrorMessages.INVALID_TYPE_PARAMETERS_COUNT, getToken());
            for (TypeVar typeVar:getTypeParams()) typeVar.validate();
        }
    }
}
