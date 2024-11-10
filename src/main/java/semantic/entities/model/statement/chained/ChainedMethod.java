package main.java.semantic.entities.model.statement.chained;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.type.PrimitiveType;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;
import java.util.Objects;

public class ChainedMethod extends Chained {
    private final List<Expression> arguments;
    private Unit method;

    public ChainedMethod(Token identifier, List<Expression> arguments, Chained chained) {
        super(identifier, chained);
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
    public Type checkType(Type type) throws SemanticException {
        if (type == null || getIdentifier() == null || arguments == null) return null;
        Class myclass = SymbolTable.getClass(type.getName());
        method = null;

        if (myclass != null) {
            String methodName = Unit.getMangledName(getIdentifier().getLexeme(), arguments.size());
            method = myclass.hasMethod(methodName)?
                myclass.getMethod(methodName):
                myclass.getAbstractMethod(methodName);
        }

        if (myclass == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_NOT_FOUND_CLASS,
                    getIdentifier().getLexeme(),
                    arguments.size(),
                    type.getName()
                ),
                getIdentifier()
            );
        } else if (method == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_NOT_FOUND,
                    getIdentifier().getLexeme(),
                    arguments.size(),
                    myclass.getName()
                ),
                getIdentifier()
            );
        } else if (myclass != SymbolTable.actualClass && method.isPrivate()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.METHOD_PRIVATE,
                    getIdentifier().getLexeme(),
                    method.getParameterCount(),
                    myclass.getName()
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

    @Override
    public void generate() {
        if (method.getReturnType() != null && !Objects.equals(method.getReturnType().getName(), PrimitiveType.VOID)) {
            SymbolTable.getGenerator().write(
                Instruction.DUP.toString(),
                Comment.RETURN_ALLOC.formatted(method.getLabel())
            );
        }
        arguments.forEach(argument -> {
            argument.generate();
            SymbolTable.getGenerator().write(
                Instruction.SWAP.toString(),
                Comment.SWAP_ARGUMENTS
            );
        });
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(),
            String.valueOf(method.getOffset()),
            Comment.VT_ACCESS_METHOD.formatted(method.getLabel())
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString(),
            Comment.CALL_METHOD.formatted(method.getLabel())
        );
        if (getChained() != null) getChained().generate();
    }

    @Override
    public boolean isVoid() {
        return method == null ||
               method.getReturnType() == null ||
               Objects.equals(method.getReturnType().getName(), PrimitiveType.VOID) ||
               (getChained() != null && getChained().isVoid());
    }
}
