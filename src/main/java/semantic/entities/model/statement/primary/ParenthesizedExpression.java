package main.java.semantic.entities.model.statement.primary;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;

public class ParenthesizedExpression extends Access {
    private final Expression expression;

    public ParenthesizedExpression(Token identifier, Expression expression) {
        super(identifier);
        this.expression = expression;
    }

    @Override
    public Type checkType() throws SemanticException {
        return null;
    }
}
