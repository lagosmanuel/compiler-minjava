package main.java.symboltable.entities.ast.primary;

import main.java.model.Token;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.Class;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.type.PrimitiveType;
import main.java.symboltable.entities.Unit;
import main.java.symboltable.entities.ast.Access;
import main.java.symboltable.entities.ast.expression.Expression;
import main.java.config.CodegenConfig;
import main.java.codegen.Comment;
import main.java.codegen.Instruction;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;
import main.java.symboltable.entities.predefined.Wrapper;

import java.util.List;
import java.util.Objects;

public class MethodAccess extends Access {
    protected Token idMethod;
    protected final List<Expression> arguments;
    protected Unit method;

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
        return checkMethodInClass(SymbolTable.actualClass, SymbolTable.actualUnit.isStatic());
    }

    protected Type checkMethodInClass(Class className, boolean static_only) throws SemanticException {
        String name = Unit.getMangledName(idMethod.getLexeme(), arguments.size());
        method = className.hasMethod(name)?
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
        } else if (!method.isStatic() && static_only) {
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

    @Override
    public void generate() {
        if (method == null || arguments == null) return;
        if (method.getReturnType() != null && !Objects.equals(method.getReturnType().getName(), PrimitiveType.VOID)) {
            SymbolTable.getGenerator().write(
                Instruction.RMEM.toString(), "1",
                Comment.RETURN_ALLOC.formatted(method.getLabel())
            );
        }
        arguments.forEach(Expression::generate);
        loadTarget();
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString(),
            Comment.CALL_METHOD.formatted(method.getLabel())
        );
        if (getChained() != null) getChained().generate();
        else Wrapper.unwrap(method.getReturnType());
    }

    protected void loadTarget() {
        if (method.isStatic()) {
            SymbolTable.getGenerator().write(
                Instruction.PUSH.toString(),
                method.getLabel(),
                Comment.ACCESS_STATIC_METHOD.formatted(method.getLabel())
            );
        } else {
            SymbolTable.getGenerator().write(
                Instruction.LOAD.toString(),
                CodegenConfig.OFFSET_THIS,
                Comment.LOAD_THIS
            );
            SymbolTable.getGenerator().write(
                Instruction.DUP.toString()
            );
            SymbolTable.getGenerator().write(
                Instruction.LOADREF.toString(), "0",
                Comment.VT_LOAD.formatted("")
            );
            SymbolTable.getGenerator().write(
                Instruction.LOADREF.toString(),
                String.valueOf(method.getOffset()),
                Comment.VT_ACCESS_METHOD.formatted(method.getLabel())
            );
        }
    }

    public boolean isVoid() {
        return method == null ||
               method.getReturnType() == null ||
               Objects.equals(method.getReturnType().getName(), PrimitiveType.VOID) ||
               (getChained() != null && getChained().isVoid());
    }
}
