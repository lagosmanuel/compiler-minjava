package main.java.symboltable.entities.ast.switchs;

import main.java.model.Token;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.ast.Switch;
import main.java.symboltable.entities.ast.Statement;
import main.java.config.CodegenConfig;
import main.java.codegen.Instruction;
import main.java.codegen.Labeler;
import main.java.exeptions.SemanticException;

abstract public class SwitchStatement {
    private final Token identifier;
    private final Statement statement;
    private boolean checked = false;
    protected String label;

    public SwitchStatement(Token identifier, Statement statement) {
        this.identifier = identifier;
        this.statement = statement;
        if (statement != null) statement.setBreakable();
    }

    public Token getIdentifier() {
        return identifier;
    }

    public String getLabel() {
        return label;
    }

    public boolean checked() {
        return checked;
    }

    public Statement getStatement() {
        return statement;
    }

    public boolean hasReturn() {
        return statement == null || statement.hasReturn();
    }

    public void check(Switch myswitch) throws SemanticException {
        checked = true;
        if (statement != null) statement.check();
    }

    public void generate() {
        if (label == null || label.isEmpty()) return;
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, label),
            Instruction.NOP.toString()
        );
        if (getStatement() != null) getStatement().generate();
    }
}
