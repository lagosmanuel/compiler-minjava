package main.java.semantic.entities.model.statement.primary;

import main.java.codegen.Comment;
import main.java.codegen.Instruction;
import main.java.config.CodegenConfig;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.Access;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class ThisAccess extends Access {
    public ThisAccess(Token identifier) {
        super(identifier);
    }

    @Override
    public boolean isAssignable() {
        return getChained() != null && getChained().isAssignable();
    }

    @Override
    public boolean isStatement() {
        return getChained() != null && getChained().isStatement();
    }

    @Override
    public Type checkType() throws SemanticException {
        Type type = Type.createType(SymbolTable.actualClass.getToken(), SymbolTable.actualClass.getTypeParameters());

        if (SymbolTable.actualUnit.isStatic()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.THIS_STATIC,
                    SymbolTable.actualUnit.getToken().getLexeme(),
                    SymbolTable.actualUnit.getParameterCount()
                ),
                getIdentifier()
            );
        }

        return getChained() != null? getChained().checkType(type):type;
    }

    @Override
    public void generate() {
        SymbolTable.getGenerator().write(
            Instruction.LOAD.toString(),
            CodegenConfig.OFFSET_THIS,
            Comment.LOAD_THIS
        );
        if (getChained() != null) getChained().generate();
    }

    @Override
    public boolean isVoid() {
        return getChained() != null && getChained().isVoid();
    }
}
