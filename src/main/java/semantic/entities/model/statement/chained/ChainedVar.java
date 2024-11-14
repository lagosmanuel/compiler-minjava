package main.java.semantic.entities.model.statement.chained;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.Attribute;
import main.java.semantic.entities.model.Type;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;
import main.java.semantic.entities.predefined.Wrapper;

public class ChainedVar extends Chained {
    private Attribute attribute;

    public ChainedVar(Token identifier, Chained chained) {
        super(identifier, chained);
    }

    @Override
    public boolean isAssignable() {
        return getChained() == null || getChained().isAssignable();
    }

    @Override
    public boolean isStatement() {
        return getChained() != null && getChained().isStatement();
    }

    @Override
    public Type checkType(Type type) throws SemanticException {
        if (type == null || getIdentifier() == null) return null;
        Class myclass = SymbolTable.getClass(type.getName());
        attribute = myclass != null? myclass.getAttribute(getIdentifier().getLexeme()):null;

        if (myclass == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.ATTRIBUTE_NOT_FOUND_CLASS,
                    getIdentifier().getLexeme(),
                    type.getName()
                ),
                getIdentifier()
            );
        } else if (attribute == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.ATTRIBUTE_NOT_FOUND,
                    getIdentifier().getLexeme(),
                    myclass.getName()
                ),
                getIdentifier()
            );
        } else if (myclass != SymbolTable.actualClass && attribute.isPrivate()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.ATTRIBUTE_PRIVATE,
                    getIdentifier().getLexeme(),
                    myclass.getName()
                ),
                getIdentifier()
            );
        } else return getChained() != null?
            getChained().checkType(attribute.getType()):
            attribute.getType();

        return null;
    }

    @Override
    public void generate() {
        if (!isLeftValue() || getChained() != null) {
            loadAttr();
            if (getChained() == null) Wrapper.unwrap(attribute.getType());
        } else {
            opPlusMinus();
            Wrapper.wrap(attribute.getType());
            storeAttr();
        }
        if (getChained() != null) getChained().generate();
    }

    private void opPlusMinus() {
        if (getAssignOp() == null) return;
        if (!getAssignOp().getType().equals(TokenType.opAssign)) {
            SymbolTable.getGenerator().write(Instruction.SWAP.toString());
            SymbolTable.getGenerator().write(Instruction.LOADSP.toString());
            SymbolTable.getGenerator().write(Instruction.LOADREF.toString(), "2");
            loadAttr();
            SymbolTable.getGenerator().write(Instruction.SWAP.toString());
        }
        if (getAssignOp().getType().equals(TokenType.opPlusAssign)) {
            SymbolTable.getGenerator().write(Instruction.ADD.toString(), Comment.ASSIGN_PLUS);
        } else if (getAssignOp().getType().equals(TokenType.opMinusAssign)) {
            SymbolTable.getGenerator().write(Instruction.SUB.toString(), Comment.ASSIGN_MINUS);
        }
        if (!getAssignOp().getType().equals(TokenType.opAssign)) {
            SymbolTable.getGenerator().write(Instruction.SWAP.toString());
        }
    }

    private void loadAttr() {
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(),
            String.valueOf(attribute.getOffset()),
            Comment.ATTRIBUTE_LOAD.formatted(getIdentifier().getLexeme())
        );
    }

    private void storeAttr() {
        SymbolTable.getGenerator().write(
            Instruction.LOADSP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "2",
            Comment.ATTRIBUTE_STORE_SWAP
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(),
            String.valueOf(attribute.getOffset()),
            Comment.ATTRIBUTE_STORE.formatted(getIdentifier().getLexeme())
        );
    }

    @Override
    public void generate(String supername) {
        generate();
    }

    @Override
    public boolean isVoid() {
        return getChained() == null || getChained().isVoid();
    }
}
