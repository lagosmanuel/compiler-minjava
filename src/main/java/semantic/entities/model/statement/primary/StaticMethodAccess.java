package main.java.semantic.entities.model.statement.primary;

import main.java.exeptions.SemanticException;
import main.java.model.Token;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;

import java.util.List;

public class StaticMethodAccess extends Access {
    private final Token idClass;
    private final Token idMethod;
    private List<Expression> arguments;

    public StaticMethodAccess(Token idClass, Token idMethod, List<Expression> arguments) {
        super(idClass);
        this.idClass = idClass;
        this.idMethod = idMethod;
        this.arguments = arguments;
    }

    @Override
    public Type checkType() throws SemanticException {
        return null;
    }
}
