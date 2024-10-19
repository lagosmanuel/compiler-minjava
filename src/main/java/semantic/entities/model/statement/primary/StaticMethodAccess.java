package main.java.semantic.entities.model.statement.primary;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;

import java.util.List;

public class StaticMethodAccess extends MethodAccess {
    private final Token idClass;

    public StaticMethodAccess(Token idClass, Token idMethod, List<Expression> arguments) {
        super(idClass, arguments);
        this.idClass = idClass;
        this.idMethod = idMethod;
    }

    @Override
    public Type checkType() throws SemanticException {
        Class myclass = SymbolTable.getClass(idClass.getLexeme());
        if (myclass == null || SymbolTable.actualClass.hasTypeParameter(idClass.getLexeme())) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CLASS_NOT_DECLARED,
                    idClass.getLexeme()
                ),
                idClass
            );
            return null;
        } else return checkMethodInClass(myclass);
    }
}
