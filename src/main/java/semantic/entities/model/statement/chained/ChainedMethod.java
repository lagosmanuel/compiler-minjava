package main.java.semantic.entities.model.statement.chained;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.Expression;

import java.util.List;

public class ChainedMethod extends Chained {
    private final List<Expression> arguments;

    public ChainedMethod(Token identifier, List<Expression> arguments, Chained chained) {
        super(identifier, chained);
        this.arguments = arguments;
    }

    @Override
    public Type checkType() throws SemanticException {
        return null;
    }
}
