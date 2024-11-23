package main.java.symboltable.entities.ast.chained;

import main.java.model.Token;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.Class;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.type.PrimitiveType;
import main.java.symboltable.entities.Unit;
import main.java.symboltable.entities.ast.expression.Expression;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;
import main.java.symboltable.entities.predefined.Wrapper;

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
        generate(null);
    }

    @Override
    public void generate(String super_vt_label) {
        if (method == null || arguments == null) return;
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
        loadMethod(super_vt_label);
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString(),
            Comment.CALL_METHOD.formatted(method.getLabel())
        );
        if (getChained() != null) getChained().generate();
        else Wrapper.unwrap(method.getReturnType());
    }

    private void loadMethod(String super_vt_label) {
        if (method.isStatic()) {
            SymbolTable.getGenerator().write(
                Instruction.POP.toString()
            );
            SymbolTable.getGenerator().write(
                Instruction.PUSH.toString(),
                method.getLabel(),
                Comment.METHOD_LOAD.formatted(method.getLabel())
            );
        } else {
            if (super_vt_label == null || super_vt_label.isEmpty()) {
                SymbolTable.getGenerator().write(
                    Instruction.DUP.toString()
                );
                SymbolTable.getGenerator().write(
                    Instruction.LOADREF.toString(), "0",
                    Comment.VT_LOAD.formatted("")
                );
            } else {
                SymbolTable.getGenerator().write(
                    Instruction.PUSH.toString(), super_vt_label,
                    Comment.VT_LOAD.formatted(super_vt_label)
                );
            }
            SymbolTable.getGenerator().write(
                Instruction.LOADREF.toString(),
                String.valueOf(method.getOffset()),
                Comment.VT_ACCESS_METHOD.formatted(method.getLabel())
            );
        }
    }

    @Override
    public boolean isVoid() {
        return method == null ||
               method.getReturnType() == null ||
               Objects.equals(method.getReturnType().getName(), PrimitiveType.VOID) ||
               (getChained() != null && getChained().isVoid());
    }
}
