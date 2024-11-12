package main.java.semantic.entities.model.statement.primary;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.Attribute;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.Block;
import main.java.config.CodegenConfig;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class VarAccess extends Access {
    private Attribute attribute;
    private int offset;

    public VarAccess(Token identifier) {
        super(identifier);
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
    public Type checkType() throws SemanticException {
        Class myclass = SymbolTable.actualClass;
        Unit unit = SymbolTable.actualUnit;
        Block block = SymbolTable.actualBlock;
        String name = getIdentifier().getLexeme();
        Type type = null;

        if (block.hasLocalVar(name)) {
            type = block.getLocalVar(name).getType();
            offset = block.getLocalVar(name).getOffset();
        } else if (unit.hasParameter(name)) {
            type = unit.getParameter(name).getType();
            offset = unit.getParameter(name).getOffset();
        } else if (myclass.hasAttribute(name)) {
            attribute = myclass.getAttribute(name);
            if (!attribute.isStatic() && unit.isStatic()) {
                SymbolTable.throwException(
                    String.format(
                        SemanticErrorMessages.ATTR_NON_STATIC_ACCESS,
                        name
                    ),
                    getIdentifier()
                );
            }
            type = attribute.getType();
        } else {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.IDENTIFIER_NOT_FOUND,
                    name
                ),
                getIdentifier()
            );
        }
        if (type == null) return null;
        else return getChained() != null? getChained().checkType(type):type;
    }

    @Override
    public void generate() {
        if (attribute != null) {
            if (!isLeftValue() || getChained() != null) {
               if (attribute.isStatic()) loadAttrStatic();
               else loadAttr();
            } else {
                if (attribute.isStatic()) {
                    opPlusMinus(true, true);
                    storeAttrStatic();
                } else {
                    opPlusMinus(true, false);
                    storeAttr();
                }
            }
        } else {
            if (!isLeftValue() || getChained() != null) loadVar();
            else {
                opPlusMinus(false, false);
                storeVar();
            }
        }
        if (getChained() != null) getChained().generate();
    }


    private void opPlusMinus(boolean isAttribute, boolean isStatic) {
        if (getAssignOp() == null) return;
        if (!getAssignOp().getType().equals(TokenType.opAssign)) {
            if (isAttribute && isStatic) loadAttrStatic();
            else if (isAttribute) loadAttr();
            else loadVar();
            SymbolTable.getGenerator().write(Instruction.SWAP.toString());
        }
        if (getAssignOp().getType().equals(TokenType.opPlusAssign)) {
            SymbolTable.getGenerator().write(Instruction.ADD.toString(), Comment.ASSIGN_PLUS);
        } else if (getAssignOp().getType().equals(TokenType.opMinusAssign)) {
            SymbolTable.getGenerator().write(Instruction.SUB.toString(), Comment.ASSIGN_MINUS);
        }
    }

    private void loadAttr() {
        SymbolTable.getGenerator().write(
            Instruction.LOAD.toString(),
            CodegenConfig.OFFSET_THIS,
            Comment.LOAD_THIS
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(),
            String.valueOf(attribute.getOffset()),
            Comment.ATTRIBUTE_LOAD.formatted(getIdentifier().getLexeme())
        );
    }

    private void loadAttrStatic() {
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            attribute.getLabel()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "0",
            Comment.ATTRIBUTE_STATIC_LOAD.formatted(getIdentifier().getLexeme())
        );
    }

    private void storeAttr() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOAD.toString(),
            CodegenConfig.OFFSET_THIS,
            Comment.LOAD_THIS
        );
        SymbolTable.getGenerator().write(
            Instruction.SWAP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(),
            String.valueOf(attribute.getOffset()),
            Comment.ATTRIBUTE_STORE.formatted(getIdentifier().getLexeme())
        );
    }

    private void storeAttrStatic() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            attribute.getLabel()
        );
        SymbolTable.getGenerator().write(
            Instruction.SWAP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(), "0",
            Comment.ATTRIBUTE_STATIC_STORE.formatted(getIdentifier().getLexeme())
        );
    }

    private void loadVar() {
        SymbolTable.getGenerator().write(
            Instruction.LOAD.toString(),
            String.valueOf(offset),
            Comment.VAR_LOAD.formatted(getIdentifier().getLexeme())
        );
    }

    private void storeVar() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.STORE.toString(),
            String.valueOf(offset),
            Comment.VAR_STORE.formatted(getIdentifier().getLexeme())
        );
    }

    @Override
    public boolean isVoid() {
        return getChained() != null && getChained().isVoid();
    }
}
