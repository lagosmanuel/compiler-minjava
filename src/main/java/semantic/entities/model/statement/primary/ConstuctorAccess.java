package main.java.semantic.entities.model.statement.primary;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.Constructor;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.semantic.entities.model.type.TypeVar;
import main.java.config.CodegenConfig;
import main.java.codegen.Comment;
import main.java.codegen.Instruction;
import main.java.codegen.Strings;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;
import main.java.semantic.entities.predefined.Wrapper;

import java.util.List;
import java.util.Objects;

public class ConstuctorAccess extends Access {
    private final Token className;
    private final List<TypeVar> typeVars;
    private final List<Expression> arguments;
    private Constructor constructor;
    private Class myclass;

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
        myclass = SymbolTable.getClass(className.getLexeme());

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
            constructor = myclass.getConstructor(mangledName);
            Type type = Type.createType(
                myclass.getToken(),
                typeVars
            );
            for (TypeVar typeVar:type.getTypeParams()) typeVar.check();
            return getChained() == null? type:getChained().checkType(type);
        }
        return null;
    }

    @Override
    public void generate() {
        if (myclass == null || constructor == null) return;
        if (!isString()) {
            allocate_result();
            if (arguments != null) arguments.forEach(Expression::generate);
            malloc_call();
            store_vt_cir();
            save_this_ref();
            call_constructor();
        } else Strings.create("");
        if (getChained() != null) getChained().generate();
        else Wrapper.unwrap(Type.createType(myclass.getToken(), myclass.getTypeParameters()));
    }

    private void allocate_result() {
        SymbolTable.getGenerator().write(
            Instruction.RMEM.toString(), "1",
            Comment.CONSTRUCTOR_ALLOC
        );
    }

    public void malloc_call() {
        SymbolTable.getGenerator().write(
            Instruction.RMEM.toString(), "1",
            Comment.RETURN_ALLOC.formatted(CodegenConfig.MALLOC_LABEL)
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            String.valueOf(myclass.getInstanceAttributes().size()+1),
            Comment.OBJECT_ALLOC.formatted(myclass.getInstanceAttributes().size())
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            CodegenConfig.MALLOC_LABEL,
            Comment.MALLOC_LOAD.formatted()
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString(),
            Comment.MALLOC_CALL.formatted()
        );
    }

    public void store_vt_cir() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(), myclass.getVTLabel(),
            Comment.VT_LOAD.formatted(myclass.getName())
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(), "0",
            Comment.VT_STORE.formatted(myclass.getName())
        );
    }

    public void save_this_ref() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADSP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.SWAP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(),
            String.valueOf(3 + (arguments != null? arguments.size():0)),
            Comment.CONSTRUCTOR_SAVE_THIS
        );
    }

    private void call_constructor() {
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            constructor.getLabel(),
            Comment.CONSTRUCTOR_LOAD.formatted(constructor.getLabel())
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString(),
            Comment.CONSTRUCTOR_CALL.formatted(constructor.getLabel())
        );
    }

    public boolean isVoid() {
        return getChained() != null && getChained().isVoid();
    }

    private boolean isString() {
        return Objects.equals(myclass.getName(), main.java.semantic.entities.predefined.String.name);
    }
}
