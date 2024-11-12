package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.config.CodegenConfig;
import main.java.codegen.Instruction;
import main.java.codegen.Labeler;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class If extends Statement {
    protected final Expression condition;
    protected final Statement then;

    public If(Token identifier, Expression condition, Statement then) {
        super(identifier);
        this.condition = condition;
        this.then = then;
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        Type conditionType = condition != null? condition.checkType():null;
        if (conditionType != null && !conditionType.isBoolean()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.IF_CONDITION_NOT_BOOLEAN,
                    conditionType.getName()
                ),
                getIdentifier()
            );
        }
        if (then != null) {
            then.setParent(getParent());
            then.check();
        }
    }

    @Override
    public void generate() {
        if (condition == null || then == null) return;
        String labelEnd = Labeler.getLabel(true, CodegenConfig.IF_END);
        condition.generate();
        SymbolTable.getGenerator().write(
            Instruction.BF.toString(),
            labelEnd
        );
        then.generate();
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, labelEnd),
            Instruction.NOP.toString()
        );
    }
}
