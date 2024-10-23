package main.java.semantic.entities.model.statement.primary;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.Constructor;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;

public class SuperAccess extends Access {
    private final Token className;
    private final List<Expression> arguments;

    @Override
    public boolean isAssignable() {
        return getChained() != null && getChained().isAssignable();
    }

    @Override
    public boolean isStatement() {
        return (arguments != null && getChained() == null) ||
               (arguments == null && getChained() != null && getChained().isStatement());
    }

    public SuperAccess(Token identifier, Token className, List<Expression> arguments) {
        super(identifier);
        this.className = className;
        this.arguments = arguments;
    }

    @Override
    public Type checkType() throws SemanticException {
        if (className == null) return null;
        Class myclass = SymbolTable.getClass(className.getLexeme());
        Class superclass = myclass != null? SymbolTable.getClass(myclass.getSuperType().getName()):null;
        Type supertype = superclass != null? Type.createType(superclass.getToken(), null):null;

        if (SymbolTable.actualUnit.isStatic()) {
            SymbolTable.throwException(
                SemanticErrorMessages.SUPER_ACCESS_STATIC,
                className
            );
            return null;
        }

        if (supertype == null) return null;

        if (arguments != null) {
            Constructor constructor = superclass.getConstructor(Unit.getMangledName(superclass.getName(), arguments.size()));
            if (constructor == null) {
                SymbolTable.throwException(
                    String.format(
                        SemanticErrorMessages.CONSTRUCTOR_NOT_FOUND,
                        arguments.size(),
                        superclass.getName()
                    ),
                    getIdentifier()
                );
            } else if (superclass != SymbolTable.actualClass && constructor.isPrivate()) {
                SymbolTable.throwException(
                    String.format(
                        SemanticErrorMessages.CONSTRUCTOR_PRIVATE,
                        arguments.size(),
                        superclass.getName()
                    ),
                    getIdentifier()
                );
            } else if (getChained() != null) {
                SymbolTable.throwException(
                    SemanticErrorMessages.SUPER_ACCESS_CHAINED,
                    getIdentifier()
                );
            } else constructor.argumentsMatch(arguments, getIdentifier());
        } else return getChained() != null? getChained().checkType(supertype):supertype;

        return null;
    }
}
