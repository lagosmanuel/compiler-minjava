package main.java.symboltable.entities.ast;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.type.PrimitiveType;
import main.java.symboltable.entities.ast.expression.Expression;
import main.java.symboltable.entities.ast.expression.Literal;
import main.java.symboltable.entities.ast.switchs.SwitchStatement;
import main.java.config.CodegenConfig;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;
import main.java.codegen.Labeler;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Switch extends Statement {
    private final Expression expression;
    private final List<SwitchStatement> statements;
    private Type expressionType = null;
    private String defaultLabel;
    private final Map<String, Literal> cases = new HashMap<>();
    private final Block body;

    public Switch(Token identifier, Expression expression, List<SwitchStatement> statements) {
        super(identifier);
        this.expression = expression;
        this.statements = statements;
        this.body = new Block(identifier);
        this.setReturnable();
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        body.setParent(getParent());
        body.check();
        body.addLocalVar(new LocalVar(
            PrimitiveType.INT_TYPE,
            List.of(new Token(TokenType.idMetVar, "", 0, 0)))
        );
        expressionType = expression != null? expression.checkType():null;
        if (expressionType == null) return;
        if (!expressionType.isInt() && !expressionType.isFloat() && !expressionType.isChar() &&
            !expressionType.isBoolean() && !expressionType.isString()) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.SWITCH_EXPRESSION_TYPE_INVALID,
                    expressionType.getName()
                ),
                getIdentifier()
            );
        } else for (SwitchStatement statement:statements) {
            statement.getStatement().setParent(body);
            statement.check(this);
            if (!statement.hasReturn()) unsetReturnable();
        }
        if (!hasDefault() ||
            statements.isEmpty() ||
            statements.getLast().getStatement() == null ||
            !statements.getLast().getStatement().hasReturn()) unsetReturnable();
    }

    @Override
    public void generate() {
        if (expression == null || statements == null || statements.isEmpty()) return;
        if (body.getParent() != null) body.allocateVars(body.getParent().getAllocatedVarsCount()+1);
        String labelEnd = Labeler.getLabel(true, CodegenConfig.SWITCH_END);
        String blockEnd = Labeler.getLabel(true, CodegenConfig.SWITCH_BLOCK_END);
        body.setLabelEnd(labelEnd);
        expression.generate();
        cases.forEach((__, literal) -> jumpCase(literal));
        jumpDefault();
        jumpEnd(blockEnd);
        statements.forEach(SwitchStatement::generate);
        endSwitch(blockEnd, labelEnd);
    }

    private void jumpCase(Literal literal) {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        literal.generate();
        if (expressionType.isString()) {
            main.java.symboltable.entities.predefined
                .String.compare();
        } else {
            SymbolTable.getGenerator().write(
                Instruction.EQ.toString()
            );
        }
        SymbolTable.getGenerator().write(
            Instruction.BT.toString(),
            literal.getLabel()
        );
    }

    private void jumpDefault() {
        if (defaultLabel == null) return;
        SymbolTable.getGenerator().write(
            Instruction.JUMP.toString(),
            defaultLabel
        );
    }

    private void jumpEnd(String label) {
        SymbolTable.getGenerator().write(
            Instruction.JUMP.toString(), label
        );
    }

    private void endSwitch(String blockEnd, String labelEnd) {
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, blockEnd),
            Instruction.FMEM.toString(),
            String.valueOf(body.ownLocalVarCount())
        );
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, labelEnd),
            Instruction.NOP.toString(),
            Comment.SWITCH_END
        );
    }

    public Type getExpressionType() {
        return expressionType;
    }

    public boolean hasDefault() {
        return defaultLabel != null;
    }

    public String setDefault() {
        return (defaultLabel = Labeler.getLabel(true, CodegenConfig.DEFAULT_LABEL));
    }

    public boolean hasCase(Literal literal) {
        if (literal == null) return false;
        return cases.containsKey(literal.getIdentifier().getLexeme());
    }

    public String addCase(Literal literal) {
        if (literal == null) return null;
        literal.setLabel(Labeler.getLabel(true, CodegenConfig.CASE_LABEL));
        cases.put(literal.getIdentifier().getLexeme(), literal);
        return literal.getLabel();
    }
}
