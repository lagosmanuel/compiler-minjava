package main.java.semantic.entities.model.statement;

import main.java.codegen.Comment;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.type.ClassType;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.CompositeExpression;
import main.java.semantic.entities.model.type.PrimitiveType;
import main.java.semantic.entities.predefined.MiniIterable;
import main.java.config.CodegenConfig;
import main.java.codegen.Instruction;
import main.java.codegen.Labeler;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;

public class ForEach extends Statement {
    private final LocalVar declaration;
    private final CompositeExpression iterable;
    private final Statement statement;
    private final Block body;

    public ForEach(Token identifier, LocalVar declaration, CompositeExpression iterable, Statement statement) {
        super(identifier);
        this.declaration = declaration;
        this.iterable = iterable;
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
        body.addLocalVar(new LocalVar(
            PrimitiveType.INT_TYPE,
            List.of(new Token(TokenType.idMetVar,  "", 0, 0))
        ));
        this.setParent(body);
        SymbolTable.actualBlock = body;

        if (declaration != null) {
            declaration.setParent(body);
            declaration.check();
        }
        Type iterableType = iterable != null? iterable.checkType():null;
        Type elementType = null;

        if (iterableType instanceof ClassType classType) {
            Type ancestor = classType.getAncestor(MiniIterable.name);
            if (ancestor != null && ancestor.getTypeParam(0) != null)
                elementType = ancestor.getTypeParam(0).getInstaceType() != null?
                    ancestor.getTypeParam(0).getInstaceType():
                    ancestor.getTypeParam(0);
        }

        if (iterableType != null && !MiniIterable.type.compatible(iterableType)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.FOREACH_NOT_ITERABLE,
                    iterableType.getName()
                ),
                getIdentifier()
            );
        } else if (declaration != null && elementType != null && !declaration.getType().compatible(elementType)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.FOREACH_TYPE_NOT_COMPATIBLE,
                    declaration.getType().getName(),
                    elementType.getName()
                ),
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
        if (declaration == null || statement == null || iterable == null) return;
        String labelEnd = Labeler.getLabel(true, CodegenConfig.FOREACH_END);
        String conditionLabel = Labeler.getLabel(true, CodegenConfig.FOREACH_CONDITION);
        body.setLabelEnd(labelEnd);
        if (body.getParent() != null) body.allocateVars(body.getParent().getAllocatedVarsCount()+1);
        iterable.generate();
        call_start();
        call_has_next(conditionLabel);
        eval_condition(labelEnd);
        call_next();
        statement.generate();
        drop_expression();
        jump_condition(conditionLabel);
        end(labelEnd);
    }


    private void call_start() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "0"
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(),
            String.valueOf(MiniIterable.METHOD_START_OFFSET)
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString()
        );
    }

    private void call_has_next(String label) {
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, label),
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.RMEM.toString(), "1"
        );
        SymbolTable.getGenerator().write(
            Instruction.SWAP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "0"
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(),
            String.valueOf(MiniIterable.METHOD_HASNEXT_OFFSET)
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString()
        );
    }

    private void eval_condition(String labelEnd) {
        SymbolTable.getGenerator().write(
            Instruction.BF.toString(), labelEnd
        );
    }

    private void call_next() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.RMEM.toString(), "1"
        );
        SymbolTable.getGenerator().write(
            Instruction.SWAP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "0"
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(),
            String.valueOf(MiniIterable.METHOD_NEXT_OFFSET)
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString()
        );
    }

    private void drop_expression() {
        SymbolTable.getGenerator().write(
            Instruction.POP.toString(),
            Comment.EXPRESSION_DROP_VALUE
        );
    }

    private void jump_condition(String label) {
        SymbolTable.getGenerator().write(
            Instruction.JUMP.toString(), label
        );
    }

    private void end(String labelEnd) {
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, labelEnd),
            Instruction.POP.toString()
        );
    }
}