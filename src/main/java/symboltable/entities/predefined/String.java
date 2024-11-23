package main.java.symboltable.entities.predefined;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.Class;
import main.java.codegen.Instruction;
import main.java.codegen.Labeler;
import main.java.config.CodegenConfig;

public class String {
    private static boolean initialized = false;
    public static final java.lang.String name = "String";

    public static final Token token = new Token(
        TokenType.idClass,
        name,
        0,
        0
    );

    private static final Class string = new Class(name, token);

    public static Class Class() {
        if (!initialized) init();
        return string;
    }

    private static void init() {
        initialized = true;
    }

    public static void compare() {
        java.lang.String condition = Labeler.getLabel(true,CodegenConfig.STRING_COMPARE_CONDITION);
        java.lang.String end = Labeler.getLabel(true,CodegenConfig.STRING_COMPARE_END);
        load_strings();
        start(condition);
        move_pointer();
        update_pointer();
        access();
        move_pointer();
        update_pointer();
        access();
        test();
        jump_end(end);
        jump_condition(condition);
        end(end);
    }

    private static void load_strings() {
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "1"
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(), "1"
        );
        SymbolTable.getGenerator().write(
            Instruction.SUB.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.SWAP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "1"
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(), "1"
        );
        SymbolTable.getGenerator().write(
            Instruction.SUB.toString()
        );
    }

    private static void start(java.lang.String label) {
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, label),
            Instruction.NOP.toString()
        );
    }

    private static void end(java.lang.String label) {
        SymbolTable.getGenerator().write(
            Labeler.getLabel(CodegenConfig.LABEL, label),
            Instruction.LOADSP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.SWAP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(), "3"
        );
        SymbolTable.getGenerator().write(
            Instruction.POP.toString()
        );
    }

    private static void move_pointer() {
        SymbolTable.getGenerator().write(
            Instruction.LOADSP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "2"
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(), "1"
        );
        SymbolTable.getGenerator().write(
            Instruction.ADD.toString()
        );
    }

    private static void update_pointer() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADSP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.SWAP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(), "4"
        );
    }

    private static void access() {
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "0"
        );
    }

    private static void test() {
        SymbolTable.getGenerator().write(
            Instruction.EQ.toString()
        );
    }

    private static void jump_end(java.lang.String label) {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.BF.toString(), label
        );
        SymbolTable.getGenerator().write(
            Instruction.POP.toString()
        );
    }

    private static void jump_condition(java.lang.String label) {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.LOADREF.toString(), "0"
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(), "0"
        );
        SymbolTable.getGenerator().write(
            Instruction.EQ.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.BF.toString(), label
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(), "1"
        );
    }
}
