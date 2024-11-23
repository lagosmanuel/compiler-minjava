package main.java.symboltable.entities.ast;

import main.java.model.Token;
import main.java.symboltable.entities.ast.chained.Chained;
import main.java.symboltable.entities.ast.expression.Operand;

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
