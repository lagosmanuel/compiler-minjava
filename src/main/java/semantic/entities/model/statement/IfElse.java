package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;
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
            elses.check();
            if (then.hasReturn() && elses.hasReturn()) setReturnable();
        }
    }
}
