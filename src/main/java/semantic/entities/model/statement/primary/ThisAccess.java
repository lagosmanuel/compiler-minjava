package main.java.semantic.entities.model.statement.primary;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.Access;

public class ThisAccess extends Access {
    Token thisName;

    public ThisAccess(Token identifier) {
        super(identifier);
        this.thisName = identifier;
    }

    @Override
    public Type checkType() throws SemanticException {
        Type type = Type.createType(SymbolTable.actualClass.getToken(), SymbolTable.actualClass.getTypeParameters());
        return getChained() == null? type:getChained().checkType(type);
    }
}
