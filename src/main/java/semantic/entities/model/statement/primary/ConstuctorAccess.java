package main.java.semantic.entities.model.statement.primary;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;

import java.util.List;

public class ConstuctorAccess extends Access {
    private final Token className;
    private final List<Expression> arguments;


    public ConstuctorAccess(Token identifier, List<Expression> arguments) {
        super(identifier);
        this.className = identifier;
        this.arguments = arguments;
    }

    @Override
    public Type checkType() throws SemanticException {
        return null;
    }
}
