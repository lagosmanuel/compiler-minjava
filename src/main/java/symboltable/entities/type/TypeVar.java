package main.java.symboltable.entities.type;

import main.java.model.Token;
import main.java.symboltable.SymbolTable;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;

public class TypeVar extends ClassType {
    protected Type instanceType;
    protected final int position;

    public TypeVar(String type_name, Token type_token, List<TypeVar> type_params, int position) {
        super(type_name, type_token, type_params);
        this.position = position;
    }

    public Type getInstaceType() {
        return instanceType;
    }

    public void setInstanceType(Type type) {
        this.instanceType = type;
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();
        updateInstanceType();
    }

    public void check() throws SemanticException {
        if (!isValidated()) validate();
        if (instanceType == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.TYPE_VAR_NOT_INSTANTIATED,
                    getName()
                ),
                getToken()
            );
        }
    }

    private void updateInstanceType() {
        if (SymbolTable.actualClass != null && SymbolTable.actualClass.hasTypeParameter(this.getName())) {
            this.setInstanceType(
                SymbolTable.actualClass.getTypeParameter(this.getName()).getInstaceType()
            );
        } else if (SymbolTable.hasClass(this.getName())) {
            this.setInstanceType(new ClassType(
                this.getName(),
                this.getToken()
            ));
        }
    }

    public int getPosition() {
        return position;
    }
}
