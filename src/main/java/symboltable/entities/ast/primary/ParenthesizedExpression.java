package main.java.symboltable.entities.ast.primary;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.ast.Access;
import main.java.symboltable.entities.ast.expression.Expression;
import main.java.symboltable.entities.predefined.Wrapper;

public class ParenthesizedExpression extends Access {
    private final Expression expression;
    private Type type;

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
        type = expression.checkType();
        return getChained() != null?
            getChained().checkType(type):type;
    }

    @Override
    public void generate() {
        if (expression == null) return;
        expression.generate();
        if (getChained() != null) {
            Wrapper.wrap(type);
            getChained().generate();
        }
    }

    @Override
    public boolean isVoid() {
        return (expression instanceof Access access && access.isVoid()) ||
               (getChained() != null && getChained().isVoid());
    }
}
