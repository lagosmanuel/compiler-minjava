package main.java.semantic.entities.model.statement.primary;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.Attribute;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.Block;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class VarAccess extends Access {

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
        } else if (unit.hasParameter(name)) {
            type = unit.getParameter(name).getType();
        } else if (myclass.hasAttribute(name)) {
            Attribute attribute = myclass.getAttribute(name);
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
}
