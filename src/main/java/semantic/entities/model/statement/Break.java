package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;

public class Break extends Statement {
    public Break(Token identifier) {
        super(identifier);
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        if (!getParent().isBreakable())
            SymbolTable.throwException(
                SemanticErrorMessages.BREAK_OUTSIDE_LOOP,
                getIdentifier()
            );
    }
}
