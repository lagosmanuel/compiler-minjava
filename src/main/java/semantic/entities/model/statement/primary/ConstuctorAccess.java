package main.java.semantic.entities.model.statement.primary;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.semantic.entities.model.type.TypeVar;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;

public class ConstuctorAccess extends Access {
    private final Token className;
    private final List<TypeVar> typeVars;
    private final List<Expression> arguments;

    public ConstuctorAccess(Token identifier, List<TypeVar> typeVars, List<Expression> arguments) {
        super(identifier);
        this.className = identifier;
        this.typeVars = typeVars;
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
        if (className == null || arguments == null) return null;

        String mangledName = Unit.getMangledName(className.getLexeme(), arguments.size());
        Class myclass = SymbolTable.getClass(className.getLexeme());

        if (myclass == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CLASS_NOT_DECLARED,
                    className.getLexeme()
                ),
                getIdentifier()
            );
        }
        else if (myclass.isAbstract()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CLASS_ABSTRACT_NEW,
                    className.getLexeme()
                ),
                getIdentifier()
            );
        } else if (typeVars != null && myclass.getTypeParametersCount() == 0) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CLASS_NOT_GENERIC,
                    className.getLexeme()
                ),
                getIdentifier()
            );
        } else if (typeVars != null && !typeVars.isEmpty() && myclass.getTypeParametersCount() != typeVars.size()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CONSTRUCTOR_WRONG_NUMBER_OF_TYPE_VARS,
                    myclass.getTypeParameters().size(),
                    typeVars.size()
                ),
                getIdentifier()
            );
        } else if (typeVars == null && myclass.getTypeParametersCount() > 0) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CONSTUCTOR_CLASS_GENERIC,
                    className.getLexeme(),
                    myclass.getTypeParametersCount()
                ),
                getIdentifier()
            );
        } else if (!myclass.hasConstructor(mangledName)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CONSTRUCTOR_NOT_DECLARED,
                    arguments.size(),
                    myclass.getName()
                ),
                getIdentifier()
            );
        } else if (SymbolTable.actualClass != myclass && myclass.getConstructor(mangledName).isPrivate()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CONSTRUCTOR_PRIVATE,
                    arguments.size(),
                    myclass.getName()
                ),
                getIdentifier()
            );
        } else if (myclass.getConstructor(mangledName).argumentsMatch(arguments, getIdentifier())) {
            Type type = Type.createType(
                myclass.getToken(),
                typeVars
            );
            return getChained() == null? type:getChained().checkType(type);
        }
        return null;
    }
}
