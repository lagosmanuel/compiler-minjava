package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.Expression;

import java.util.List;

public class LocalVar extends Statement {
    private final Type type;
    private final List<Token> identifiers;
    private final Expression value;

    public LocalVar(Type type, List<Token> identifiers, Expression value) {
        super(type.getToken()); // TODO
        this.type = type;
        this.identifiers = identifiers;
        this.value = value;
    }

    public LocalVar(Type type, List<Token> identifiers) {
        super(identifiers.getFirst());
        this.type = type;
        this.identifiers = identifiers;
        this.value = null;
    }

    @Override
    public void check() {

    }
}
