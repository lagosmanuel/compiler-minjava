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
    public boolean isAssignable() {
        return expression != null &&
            ((expression.isAssignable() && getChained() == null) ||
            (getChained() != null && getChained().isAssignable()));
    }

    @Override
    public boolean isStatement() {
        return expression != null &&
            (getChained() != null && getChained().isStatement());
    }

    @Override
    public Type checkType() throws SemanticException {
        if (expression == null) return null;
        return getChained() != null?
            getChained().checkType(expression.checkType()):
            expression.checkType();
    }

    @Override
    public void generate() {
        if (expression == null) return;
        expression.generate();
        if (getChained() != null) getChained().generate();
    }
}
