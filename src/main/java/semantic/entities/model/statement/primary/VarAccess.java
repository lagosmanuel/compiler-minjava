package main.java.semantic.entities.model.statement.primary;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Attribute;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.model.statement.Access;
import main.java.semantic.entities.model.statement.Block;

public class VarAccess extends Access {
    private final Token varName;

    public VarAccess(Token identifier) {
        super(identifier);
        this.varName = identifier;
    }

    @Override
    public Type checkType() throws SemanticException {
        Class myclass = SymbolTable.actualClass;
        Unit unit = SymbolTable.actualUnit;
        Block body = unit.getBody();
        Type type = null;

        if (body.hasLocalVar(varName.getLexeme())) {
            type = body.getLocalVar(varName.getLexeme()).getType();
        } else if (unit.hasParameter(varName.getLexeme())) {
            type = unit.getParameter(varName.getLexeme()).getType();
        } else if (myclass.hasAttribute(varName.getLexeme())) {
            Attribute attribute = myclass.getAttribute(varName.getLexeme());
            if (!attribute.isStatic() && unit.isStatic())
                SymbolTable.throwException(
                    String.format(
                        SemanticErrorMessages.ATTR_NON_STATIC_ACCESS,
                        varName.getLexeme()
                    ),
                    varName
                );
            type = attribute.getType();
        } else {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.IDENTIFIER_NOT_FOUND,
                    varName.getLexeme()
                ),
                varName
            );
        }
        if (type == null) return null;
        else return getChained() != null? getChained().checkType(type):type;
    }
}
