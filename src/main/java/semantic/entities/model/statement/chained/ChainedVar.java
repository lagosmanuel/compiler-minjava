package main.java.semantic.entities.model.statement.chained;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.Attribute;
import main.java.semantic.entities.model.Type;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class ChainedVar extends Chained {
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
        Attribute attr = myclass != null? myclass.getAttribute(getIdentifier().getLexeme()):null;

        if (myclass == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.ATTRIBUTE_NOT_FOUND_CLASS,
                    getIdentifier().getLexeme(),
                    type.getName()
                ),
                getIdentifier()
            );
        } else if (attr == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.ATTRIBUTE_NOT_FOUND,
                    getIdentifier().getLexeme(),
                    myclass.getName()
                ),
                getIdentifier()
            );
        } else if (myclass != SymbolTable.actualClass && attr.isPrivate()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.ATTRIBUTE_PRIVATE,
                    getIdentifier().getLexeme(),
                    myclass.getName()
                ),
                getIdentifier()
            );
        } else return getChained() != null?
            getChained().checkType(attr.getType()):
            attr.getType();

        return null;
    }
}
