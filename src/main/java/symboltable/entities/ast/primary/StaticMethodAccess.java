package main.java.symboltable.entities.ast.primary;

import main.java.model.Token;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.Class;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.ast.expression.Expression;
import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;

import java.util.List;

public class StaticMethodAccess extends MethodAccess {
    private final Token idClass;

    public StaticMethodAccess(Token idClass, Token idMethod, List<Expression> arguments) {
        super(idMethod, arguments);
        this.idClass = idClass;
        this.idMethod = idMethod;
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
        if (idClass == null) return null;
        Class myclass = SymbolTable.getClass(idClass.getLexeme());
        if (myclass == null || SymbolTable.actualClass.hasTypeParameter(idClass.getLexeme())) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CLASS_NOT_DECLARED,
                    idClass.getLexeme()
                ),
                getIdentifier()
            );
            return null;
        } else return checkMethodInClass(myclass, true);
    }
}
