package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.type.PrimitiveType;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;
import main.java.semantic.entities.predefined.Wrapper;

import java.util.Objects;

public class Return extends Statement {
    private final Expression expression;
    private Type declaredType;

    public Return(Token identifier, Expression expression) {
        super(identifier);
        this.expression = expression;
        this.setReturnable();
    }

    public Return(Token identifier) {
        super(identifier);
        this.expression = null;
        this.setReturnable();
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        Type returnType = expression != null? expression.checkType():null;
        this.declaredType = SymbolTable.actualUnit.getReturnType();

        if (returnType != null && SymbolTable.actualUnit.getReturnType() == null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.RETURN_UNEXPECTED
                ),
                getIdentifier()
            );
        } else if (returnType != null && !SymbolTable.actualUnit.getReturnType().compatible(returnType)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.RETURN_TYPE_MISMATCH,
                    SymbolTable.actualUnit.getReturnType().getName(),
                    returnType.getName()
                ),
                getIdentifier()
            );
        } else if (returnType == null && SymbolTable.actualUnit.getReturnType() != null &&
                  !Objects.equals(PrimitiveType.VOID, SymbolTable.actualUnit.getReturnType().getName())) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.RETURN_NULL,
                    SymbolTable.actualUnit.getReturnType().getName()
                ),
                getIdentifier()
            );
        }
    }

    @Override
    public void generate() {
        if (expression != null) {
            expression.generate();
            Wrapper.wrap(declaredType);
            SymbolTable.getGenerator().write(
                Instruction.STORE.toString(),
                String.valueOf(SymbolTable.actualUnit.getReturnOffset()),
                Comment.RETURN_STORE
            );
        }
        SymbolTable.getGenerator().write(
            Instruction.FMEM.toString(),
            String.valueOf(getParent().getAllocatedVarsCount()),
            Comment.BLOCK_RET
        );
        SymbolTable.getGenerator().write(
            Instruction.JUMP.toString(),
            SymbolTable.actualUnit.getEpilogueLabel(),
            Comment.UNIT_EPILOGUE_JUMP.formatted(SymbolTable.actualUnit.getLabel())
        );
    }
}
