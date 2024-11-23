package main.java.symboltable.entities.ast.switchs;

import main.java.model.Token;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.ast.Statement;
import main.java.symboltable.entities.ast.Switch;
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