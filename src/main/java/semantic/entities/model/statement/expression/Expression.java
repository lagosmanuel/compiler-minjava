package main.java.semantic.entities.model.statement.expression;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;

abstract public class Expression extends Statement {
    public Expression(Token identifier) {
        super(identifier);
    }

    public abstract Type checkType() throws SemanticException;

    public void check() throws SemanticException {
        checkType();
    }
}
