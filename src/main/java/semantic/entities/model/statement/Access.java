package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.entities.model.statement.chained.Chained;
import main.java.semantic.entities.model.statement.expression.Operand;

public abstract class Access extends Operand {
    private Chained chained;

    public Access(Token identifier) {
        super(identifier);
    }

    public Chained getChained() {
        return chained;
    }

    public void setChained(Chained chained) {
        this.chained = chained;
    }

    @Override
    public void setAssignOp(Token assignOp) {
        this.assignOp = assignOp;
        if (chained != null) chained.setAssignOp(assignOp);
    }

    public abstract boolean isVoid();
}
