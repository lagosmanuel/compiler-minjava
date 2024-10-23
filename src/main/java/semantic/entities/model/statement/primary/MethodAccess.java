package main.java.semantic.entities.model.statement.primary;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;

public class MethodAccess extends Access {
    protected Token idMethod;
    protected final List<Expression> arguments;

    public MethodAccess(Token identifier, List<Expression> arguments) {
        super(identifier);
        this.idMethod = identifier;
        this.arguments = arguments;
    }

    @Override
    public boolean isAssignable() {
        return getChained() != null && getChained().isAssignable();
    }

    @Override
    public boolean isStatement() {
        return getChained() == null || getChained().isStatement();
    }

    @Override
    public Type checkType() throws SemanticException {
        return checkMethodInClass(SymbolTable.actualClass);
    }

    protected Type checkMethodInClass(Class className) throws SemanticException {
        String name = Unit.getMangledName(idMethod.getLexeme(), arguments.size());
        Unit method = className.hasMethod(name)?
            className.getMethod(name):
            className.getAbstractMethod(name);

        if (method == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_NOT_FOUND,
                    idMethod.getLexeme(),
                    arguments.size(),
                    className.getName()
                ),
                getIdentifier()
            );
        } else if (className != SymbolTable.actualClass && method.isPrivate()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_PRIVATE,
                    idMethod.getLexeme(),
                    method.getParameterCount(),
                    className.getName()
                ),
                getIdentifier()
            );
        } else if (!method.isStatic() && SymbolTable.actualUnit.isStatic()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_NON_STATIC,
                    idMethod.getLexeme(),
                    method.getParameterCount()
                ),
                getIdentifier()
            );
        } else if (method.argumentsMatch(arguments, getIdentifier())) {
            return getChained() != null?
                getChained().checkType(method.getReturnType()):
                method.getReturnType();
        }
        return null;
    }
}
