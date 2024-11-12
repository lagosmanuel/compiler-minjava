package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.config.CodegenConfig;
import main.java.codegen.Instruction;
import main.java.codegen.Labeler;
import main.java.exeptions.SemanticException;

public class IfElse extends If {
    protected final Statement elses;

    public IfElse(Token identifier, Expression condition, Statement then, Statement elses) {
        super(identifier, condition, then);
        this.elses = elses;
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        if (elses != null) {
            elses.setParent(getParent());
            elses.check();
            if (then.hasReturn() && elses.hasReturn()) setReturnable();
        }
    }

    @Override
    public void generate() {
        if (condition == null || then == null || elses == null) return;
        String labelEnd = Labeler.getLabel(true, CodegenConfig.IF_END);
        String labelElse = Labeler.getLabel(true, CodegenConfig.IF_ELSE);
        condition.generate();
        SymbolTable.getGenerator().write(
            Instruction.BF.toString(),
            labelElse
        );
        then.generate();
        SymbolTable.getGenerator().write(
            Instruction.JUMP.toString(),
            labelEnd
        );
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, labelElse),
            Instruction.NOP.toString()
        );
        elses.generate();
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, labelEnd),
            Instruction.NOP.toString()
        );
    }
}
