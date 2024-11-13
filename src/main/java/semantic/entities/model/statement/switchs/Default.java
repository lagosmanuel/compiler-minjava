package main.java.semantic.entities.model.statement.switchs;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.Switch;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class Default extends SwitchStatement {
    public Default(Token identifier, Statement statement) {
        super(identifier, statement);
    }

    @Override
    public void check(Switch myswitch) throws SemanticException {
        if (checked() || myswitch == null) return;
        super.check(myswitch);
        if (myswitch.hasDefault()) {
            SymbolTable.throwException(
                SemanticErrorMessages.SWITCH_DEFAULT_ALREADY_DEFINED,
                getIdentifier()
            );
        } else label = myswitch.setDefault();
    }
}