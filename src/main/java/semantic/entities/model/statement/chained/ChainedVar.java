package main.java.semantic.entities.model.statement.chained;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;

public class ChainedVar extends Chained{
    public ChainedVar(Token identifier, Chained chained) {
        super(identifier, chained);
    }

    @Override
    public Type checkType() throws SemanticException {
        return null;
    }
}
