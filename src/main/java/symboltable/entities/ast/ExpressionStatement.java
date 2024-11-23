package main.java.symboltable.entities.ast;

import main.java.codegen.Instruction;
import main.java.codegen.Comment;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.ast.expression.Expression;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class ExpressionStatement extends Statement {
    private final Expression expression;

    public ExpressionStatement(Expression expression) {
        super(expression != null? expression.getIdentifier():null);
        this.expression = expression;
    }

    @Override
    public void check() throws SemanticException {
        if (expression == null) return;
        expression.checkType();
        if (!expression.isStatement()) {
            SymbolTable.throwException(
                SemanticErrorMessages.EXPRESSION_NOT_STATEMENT,
                getIdentifier()
            );
        }
    }

    @Override
    public void generate() {
        if (expression == null) return;
        expression.generate();
        if (expression instanceof Access access && !access.isVoid() ||
            expression instanceof Assignment) {
            SymbolTable.getGenerator().write(
                Instruction.POP.toString(),
                Comment.EXPRESSION_DROP_VALUE
            );
        }
    }

    public Expression getExpression() {
        return expression;
    }
}
