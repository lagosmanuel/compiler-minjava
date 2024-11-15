package main.java.semantic.entities.model.statement;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;
import main.java.messages.CodegenErrorMessages;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;

public class Break extends Statement {
    public Break(Token identifier) {
        super(identifier);
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        if (!isBreakable()) {
            SymbolTable.throwException(
                SemanticErrorMessages.BREAK_OUTSIDE_LOOP,
                getIdentifier()
            );
        }
    }

    @Override
    public void generate() {
        Block block = getParent();
        while (block != null && block.getLabelEnd() == null)
            block = block.getParent();
        if (block == null) throw new RuntimeException(CodegenErrorMessages.BREAK_NOTFOUND);

        SymbolTable.getGenerator().write(
            Instruction.FMEM.toString(), String.valueOf(block.getParent() != null?
                getParent().getAllocatedVarsCount()-block.getParent().getAllocatedVarsCount():
                getParent().getAllocatedVarsCount()
            ),
            Comment.BLOCK_BREAK
        );
        SymbolTable.getGenerator().write(
            Instruction.JUMP.toString(),
            block.getLabelEnd(),
            Comment.BREAK_JUMP
        );
    }
}
