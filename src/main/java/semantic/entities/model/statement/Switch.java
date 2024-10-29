package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.semantic.entities.model.statement.expression.Literal;
import main.java.semantic.entities.model.statement.switchs.SwitchStatement;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Switch extends Statement {
    private final Expression expression;
    private final List<SwitchStatement> statements;
    private Type expressionType = null;
    private boolean hasDefault = false;
    private final Set<String> cases = new HashSet<>();

    public Switch(Token identifier, Expression expression, List<SwitchStatement> statements) {
        super(identifier);
        this.expression = expression;
        this.statements = statements;
        this.setReturnable();
    }

    @Override
    public void check() throws SemanticException {
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
            statement.check(this);
            if (!statement.hasReturn()) unsetReturnable();
        }
        if (!hasDefault ||
            statements.isEmpty() ||
            statements.getLast().getStatement() == null ||
            !statements.getLast().getStatement().hasReturn()) unsetReturnable();
    }

    public Type getExpressionType() {
        return expressionType;
    }

    public boolean hasDefault() {
        return hasDefault;
    }

    public void setDefault() {
        hasDefault = true;
    }

    public boolean hasCase(Literal literal) {
        if (literal == null) return false;
        return cases.contains(literal.getIdentifier().getLexeme());
    }

    public void addCase(Literal literal) {
        if (literal == null) return;
        cases.add(literal.getIdentifier().getLexeme());
    }
}
