package main.java.semantic.entities.model.statement.chained;

import main.java.config.SemanticConfig;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.exeptions.SemanticException;

import java.util.List;

public class ChainedMethod extends Chained {
    private final List<Expression> arguments;

    public ChainedMethod(Token identifier, List<Expression> arguments, Chained chained) {
        super(identifier, chained);
        this.arguments = arguments;
    }

    @Override
    public Type checkType(Type type) throws SemanticException {
        if (type == null) return null;
        Class myclass = SymbolTable.getClass(type.getName());
        String name = getMangledName();
        Unit method = myclass != null? myclass.getMethod(name):null;
        if (method == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_NOT_FOUND,
                    getIdentifier().getLexeme(),
                    arguments.size(),
                    myclass != null? myclass.getName():"null"
                ),
                getIdentifier()
            );
        } else if (myclass != SymbolTable.actualClass && method.isPrivate()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_PRIVATE,
                    getIdentifier().getLexeme(),
                    myclass.getName()
                ),
                getIdentifier()
            );
        } else if (method.getParameterCount() != arguments.size()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_WRONG_NUMBER_OF_ARGUMENTS,
                    getIdentifier().getLexeme(),
                    myclass.getName(),
                    method.getParameterCount(),
                    arguments.size()
                ),
                getIdentifier()
            );
        } else if (areArgumentsCompatible(method)) {
            return getChained() != null?
                    getChained().checkType(method.getReturn()):
                    method.getReturn();
        }
        return null;
    }

    private String getMangledName() {
        String separator = !arguments.isEmpty()? SemanticConfig.PARAMETER_TYPE_SEPARATOR:"";
        String counter = "X".repeat(arguments.size());
        return getIdentifier().getLexeme() + separator + counter;
    }

    private boolean areArgumentsCompatible(Unit method) throws SemanticException {
        if (method == null) return false;
        boolean compatible = true;
        for (int i = 0; i < arguments.size(); ++i) {
            Type argumentType = arguments.get(i).checkType();
            Type paramType = method.getParameter(i).getType();
            if (!paramType.compatible(argumentType)) {
                compatible = false;
                SymbolTable.throwException(
                        String.format(
                                SemanticErrorMessages.TYPE_NOT_COMPATIBLE,
                                paramType.getName(),
                                argumentType != null? argumentType.getName():"null"
                        ),
                        getIdentifier()
                );
            }
        }
        return compatible;
    }
}
