package main.java.symboltable.entities.ast.switchs;

import main.java.model.Token;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.ast.Statement;
import main.java.symboltable.entities.ast.Switch;
import main.java.symboltable.entities.ast.expression.Literal;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class Case extends SwitchStatement {
    private final Literal literal;

    public Case(Token identifier, Literal literal, Statement statement) {
        super(identifier, statement);
        this.literal = literal;
    }

    @Override
    public void check(Switch myswitch) throws SemanticException {
        if (checked() || myswitch == null || myswitch.getExpressionType() == null) return;
        super.check(myswitch);
        Type literalType = literal != null? literal.checkType():null;
        if (literalType == null) return;
        if (!myswitch.getExpressionType().compatible(literalType)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CASE_EXPRESSION_TYPE_NOT_COMPATIBLE,
                    myswitch.getExpressionType().getName(),
                    literalType.getName()
                ),
                literal.getIdentifier()
            );
        } else if (myswitch.hasCase(literal)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CASE_ALREADY_DEFINED,
                    literal.getIdentifier().getLexeme()
                ),
                literal.getIdentifier()
            );
        } else label = myswitch.addCase(literal);
    }
}
