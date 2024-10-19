package main.java.semantic.entities.model.statement.primary;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.config.SemanticConfig;
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
    public Type checkType() throws SemanticException {
        return checkMethodInClass(SymbolTable.actualClass);
    }

    protected Type checkMethodInClass(Class className) throws SemanticException {
        String name = getMangledName();
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
                idMethod
            );
        } else if (className != SymbolTable.actualClass && method.isPrivate()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_PRIVATE,
                    idMethod.getLexeme(),
                    className.getName()
                ),
                idMethod
            );
        } else if (method.getParameterCount() != arguments.size()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_WRONG_NUMBER_OF_ARGUMENTS,
                    idMethod.getLexeme(),
                    className.getName(),
                    method.getParameterCount(),
                    arguments.size()
                ),
                    idMethod
            );
        } else if (!method.isStatic() && SymbolTable.actualUnit.isStatic()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_NON_STATIC,
                    idMethod.getLexeme()
                ),
                    idMethod
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
        return idMethod.getLexeme() + separator + counter;
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
                        idMethod
                );
            }
        }
        return compatible;
    }
}
