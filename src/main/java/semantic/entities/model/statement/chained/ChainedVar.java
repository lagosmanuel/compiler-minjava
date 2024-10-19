package main.java.semantic.entities.model.statement.chained;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Attribute;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Type;

public class ChainedVar extends Chained{
    public ChainedVar(Token identifier, Chained chained) {
        super(identifier, chained);
    }

    @Override
    public Type checkType(Type type) throws SemanticException {
        if (type == null) return null;
        Class myclass = SymbolTable.getClass(type.getName());
        Attribute attr = myclass != null? myclass.getAttribute(getIdentifier().getLexeme()):null;
        if (attr == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.ATTRIBUTE_NOT_FOUND,
                    getIdentifier().getLexeme(),
                    myclass != null? myclass.getName():"null"
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
        } else return getChained() != null? getChained().checkType(attr.getType()):attr.getType();
        return null;
    }
}
