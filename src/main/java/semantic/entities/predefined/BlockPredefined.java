package main.java.semantic.entities.predefined;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.statement.Block;
import main.java.semantic.entities.model.statement.Return;
import main.java.semantic.entities.model.statement.expression.Literal;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;

public class BlockPredefined extends Block {
    private final MethodPredefined method;

    public BlockPredefined(MethodPredefined method) {
        super(new Token(
            main.java.model.TokenType.leftBrace,
            "{",
            0,
            0
        ));
        this.method = method;
        if (method.equals(MethodPredefined.read)) addReturn0();
    }

    @Override
    public void generate() {
        switch (method) {
            case debugPrint, printIln -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOAD.toString(),
                    "3",
                    Comment.VAR_LOAD.formatted("i")
                );
                SymbolTable.getGenerator().write(
                    Instruction.IPRINT.toString(),
                    Comment.IPRINT
                );
                SymbolTable.getGenerator().write(
                    Instruction.PRNLN.toString(),
                    Comment.PRNLN
                );
            }
            case read -> {
                SymbolTable.getGenerator().write(
                    Instruction.READ.toString(),
                    Comment.READ
                );
                SymbolTable.getGenerator().write(
                    Instruction.STORE.toString(),
                    "3",
                    Comment.RETURN_STORE
                );
            }
            case printB -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOAD.toString(),
                    "3",
                    Comment.VAR_LOAD.formatted("b")
                );
                SymbolTable.getGenerator().write(
                    Instruction.BPRINT.toString(),
                    Comment.BPRINT
                );
            }
            case printC -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOAD.toString(),
                    "3",
                    Comment.VAR_LOAD.formatted("c")
                );
                SymbolTable.getGenerator().write(
                    Instruction.CPRINT.toString(),
                    Comment.CPRINT
                );
            }
            case printI -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOAD.toString(),
                    "3",
                    Comment.VAR_LOAD.formatted("i")
                );
                SymbolTable.getGenerator().write(
                    Instruction.IPRINT.toString(),
                    Comment.IPRINT
                );
            }
            case printS -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOAD.toString(),
                    "3",
                    Comment.VAR_LOAD.formatted("s")
                );
                SymbolTable.getGenerator().write(
                    Instruction.LOADREF.toString(),
                    "1",
                    Comment.STRING_LOAD
                );
                SymbolTable.getGenerator().write(
                    Instruction.SPRINT.toString(),
                    Comment.SPRINT
                );
            }
            case println -> {
                SymbolTable.getGenerator().write(
                    Instruction.PRNLN.toString(),
                    Comment.PRNLN
                );
            }
            case printBln -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOAD.toString(),
                    "3",
                    Comment.VAR_LOAD.formatted("b")
                );
                SymbolTable.getGenerator().write(
                    Instruction.BPRINT.toString(),
                    Comment.BPRINT
                );
                SymbolTable.getGenerator().write(
                    Instruction.PRNLN.toString(),
                    Comment.PRNLN
                );
            }
            case printCln -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOAD.toString(),
                    "3",
                    Comment.VAR_LOAD.formatted("c")
                );
                SymbolTable.getGenerator().write(
                    Instruction.CPRINT.toString(),
                    Comment.CPRINT
                );
                SymbolTable.getGenerator().write(
                    Instruction.PRNLN.toString(),
                    Comment.PRNLN
                );
            }
            case printSln -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOAD.toString(),
                    "3",
                    Comment.VAR_LOAD.formatted("s")
                );
                SymbolTable.getGenerator().write(
                    Instruction.LOADREF.toString(),
                    "1",
                    Comment.STRING_LOAD
                );
                SymbolTable.getGenerator().write(
                    Instruction.SPRINT.toString(),
                    Comment.SPRINT
                );
                SymbolTable.getGenerator().write(
                    Instruction.PRNLN.toString(),
                    Comment.PRNLN
                );
            }
        }
    }

    private void addReturn0() {
        addStatement(new Return(
            new Token(
                TokenType.kwReturn,
                "return",
                0,
                0
            ),
            new Literal(
                new Token(
                    TokenType.intLiteral,
                    "0",
                    0,
                    0
                )
            )
        ));
    }
}
