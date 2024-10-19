package main.java.semantic.entities.model.statement.primary;

import main.java.config.SemanticConfig;
import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.semantic.entities.model.type.TypeVar;

import java.util.List;

public class ConstuctorAccess extends Access {
    private final Token className;
    private final Token genericId;
    private final List<TypeVar> typeVars;
    private final List<Expression> arguments;


    public ConstuctorAccess(Token identifier, Token genericId, List<TypeVar> typeVars, List<Expression> arguments) {
        super(identifier);
        this.className = identifier;
        this.genericId = genericId;
        this.typeVars = typeVars;
        this.arguments = arguments;
    }

    @Override
    public Type checkType() throws SemanticException {
        String separator = arguments.isEmpty()? "":SemanticConfig.PARAMETER_TYPE_SEPARATOR;
        String counter = "X".repeat(arguments.size());
        String mangledName = className.getLexeme() + separator + counter;
        if (!SymbolTable.hasClass(className.getLexeme()))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CLASS_NOT_DECLARED,
                    className.getLexeme()
                ),
                className
            );
        else if (typeVars != null && SymbolTable.getClass(className.getLexeme()).getTypeParameters().size() != typeVars.size())
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CONSTRUCTOR_WRONG_NUMBER_OF_TYPE_VARS,
                    SymbolTable.getClass(className.getLexeme()).getTypeParameters().size(),
                    typeVars.size()
                ),
                className
            );
        else if (!SymbolTable.getClass(className.getLexeme()).hasConstructor(mangledName))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CONSTRUCTOR_NOT_DECLARED,
                    arguments.size()
                ),
                className
            );
        else {
            Type type = Type.createType(
                SymbolTable.getClass(className.getLexeme()).getToken(),
                SymbolTable.getClass(className.getLexeme()).getTypeParameters()
            );
            return getChained() == null? type:getChained().checkType(type);
        }
        return null;
    }
}
