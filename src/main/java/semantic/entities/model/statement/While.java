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

public class While extends Statement {
    private final Expression condition;
    private final Statement statement;

    public While(Token identifier, Expression condition, Statement statement) {
        super(identifier);
        this.condition = condition;
        this.statement = statement;
        if (statement != null) {
            statement.setBreakable();
            statement.setParent(getParent());
        };
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        Type conditionType = condition != null? condition.checkType():null;
        if (conditionType != null && !conditionType.isBoolean()) {
            SymbolTable.throwException(
                SemanticErrorMessages.WHILE_CONDITION_NOT_BOOLEAN,
                getIdentifier()
            );
        }
        if (statement != null) statement.check();
    }

    @Override
    public void generate() {
        if (condition == null || statement == null) return;
        String labelCondition = Labeler.getLabel(true, CodegenConfig.WHILE_CONDITION);
        String labelEnd = Labeler.getLabel(true, CodegenConfig.WHILE_END);
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, labelCondition),
            Instruction.NOP.toString()
        );
        condition.generate();
        SymbolTable.getGenerator().write(
            Instruction.BF.toString(),
            labelEnd
        );
        statement.generate();
        SymbolTable.getGenerator().write(
            Instruction.JUMP.toString(),
            labelCondition
        );
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, labelEnd),
            Instruction.NOP.toString()
        );
    }
}
