package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.CompositeExpression;

public class ForEach extends Statement {
    private final LocalVar declaration;
    private final CompositeExpression iterable;
    private final Statement body;

    public ForEach(Token identifier, LocalVar declaration, CompositeExpression iterable, Statement body) {
        super(identifier);
        this.declaration = declaration;
        this.iterable = iterable;
        this.body = body;
        if (body != null) body.setBreakable();
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        declaration.check();
        Type iterableType = iterable.checkType();
        if (!declaration.getType().compatible(iterableType))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.TYPE_NOT_COMPATIBLE,
                    declaration.getType().getName(),
                    iterableType != null? iterableType.getName():"null"
                ),
                getIdentifier()
            );
        if (body != null) body.check();
    }
}