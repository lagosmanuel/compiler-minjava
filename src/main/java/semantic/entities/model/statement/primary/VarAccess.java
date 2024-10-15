package main.java.semantic.entities.model.statement.primary;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.Access;

public class VarAccess extends Access {
    private final Token varName;

    public VarAccess(Token identifier) {
        super(identifier);
        this.varName = identifier;
    }

    @Override
    public Type checkType() throws SemanticException {
        return null;
    }
}
