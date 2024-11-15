package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.CompositeExpression;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.config.CodegenConfig;
import main.java.codegen.Instruction;
import main.java.codegen.Labeler;
import main.java.codegen.Comment;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class For extends Statement {
    private final Statement assignment;
    private final CompositeExpression condition;
    private final Expression increment;
    private final Statement statement;
    private final Block body;

    public For(Token identifier, Statement assignment, CompositeExpression condition, Expression increment, Statement statement) {
        super(identifier);
        this.assignment = assignment;
        this.condition = condition;
        this.increment = increment;
        this.statement = statement;
        this.body = new Block(identifier);
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();

        body.setBreakable();
        body.setParent(getParent());
        body.check();
        this.setParent(body);
        SymbolTable.actualBlock = body;

        if (assignment != null) {
            assignment.setParent(body);
            assignment.check();
        }

        Type conditionType = condition != null? condition.checkType():null;
        Type incrementType = increment != null? increment.checkType():null;

        if (conditionType != null && !conditionType.isBoolean()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.FOR_CONDITION_NOT_BOOLEAN,
                    conditionType.getName()
                ),
                getIdentifier()
            );
        }

        if (increment != null && !(increment instanceof Assignment)) {
            SymbolTable.throwException(
                SemanticErrorMessages.FOR_INCREMENT_NOT_ASSIGNMENT,
                getIdentifier()
            );
        }

        if (statement != null) {
            statement.setParent(body);
            statement.check();
        }

        this.setParent(body.getParent());
        SymbolTable.actualBlock = body.getParent();
    }

    @Override
    public void generate() {
        String labelEnd = Labeler.getLabel(true, CodegenConfig.FOR_END);
        String blockEnd = Labeler.getLabel(true, CodegenConfig.FOR_BLOCK_END);
        String labelCondition = Labeler.getLabel(true, CodegenConfig.FOR_CONDITION);
        body.setLabelEnd(labelEnd);
        init();
        condition(labelCondition);
        jumpEnd(blockEnd);
        statement.generate();
        increment();
        jumpCondition(labelCondition);
        end(blockEnd, labelEnd);
    }

    private void init() {
        assignment.generate();
    }

    private void condition(String label) {
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, label),
            Instruction.NOP.toString()
        );
        condition.generate();
    }

    private void increment() {
        increment.generate();
        SymbolTable.getGenerator().write(
            Instruction.POP.toString(),
            Comment.EXPRESSION_DROP_VALUE
        );
    }

    private void jumpCondition(String label) {
        SymbolTable.getGenerator().write(
            Instruction.JUMP.toString(), label
        );
    }

    private void jumpEnd(String label) {
        SymbolTable.getGenerator().write(
            Instruction.BF.toString(), label
        );
    }

    private void end(String blockEnd, String labelEnd) {
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, blockEnd),
            Instruction.FMEM.toString(),
            String.valueOf(body.ownLocalVarCount())
        );
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, labelEnd),
            Instruction.NOP.toString()
        );
    }
}
