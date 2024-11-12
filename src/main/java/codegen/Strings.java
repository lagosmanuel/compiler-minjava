package main.java.codegen;

import main.java.config.CodegenConfig;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;

import java.util.Map;
import java.util.HashMap;

public class Strings {
    private static Class myClass = main.java.semantic.entities.predefined.String.Class();
    private static final Map<String, String> strings = new HashMap<>();

    public static void reset() {
        strings.clear();
    }

    private static String register(String string) {
        if (!strings.containsKey(string))
            strings.put(string, Labeler.getLabel(true, CodegenConfig.STRING_LABEL));
        return strings.get(string);
    }

    public static void create(String string) {
        String label = register(string);
        allocate_result();
        malloc_call();
        store_vt_cir();
        save_this_ref();
        call_constructor();
        store_ref(label);
    }
    private static void allocate_result() {
        SymbolTable.getGenerator().write(
            Instruction.RMEM.toString(), "1",
            Comment.CONSTRUCTOR_ALLOC
        );
    }

    public static void malloc_call() {
        SymbolTable.getGenerator().write(
        Instruction.RMEM.toString(), "1",
            Comment.RETURN_ALLOC.formatted(CodegenConfig.MALLOC_LABEL)
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            String.valueOf(2),
            Comment.OBJECT_ALLOC.formatted(1)
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            CodegenConfig.MALLOC_LABEL,
            Comment.MALLOC_LOAD.formatted()
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString(),
            Comment.MALLOC_CALL.formatted()
        );
    }

    public static void store_vt_cir() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            CodegenConfig.VT_FORMAT.formatted(myClass.getName()),
            Comment.VT_LOAD.formatted(myClass.getName())
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(), "0",
            Comment.VT_STORE.formatted(myClass.getName())
        );
    }

    public static void save_this_ref() {
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
            Instruction.STOREREF.toString(),
            String.valueOf(3),
            Comment.CONSTRUCTOR_SAVE_THIS
        );
    }

    private static void call_constructor() {
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            myClass.getConstructor(myClass.getName()).getLabel(),
            Comment.CONSTRUCTOR_LOAD.formatted(myClass.getConstructor(myClass.getName()).getLabel())
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString(),
            Comment.CONSTRUCTOR_CALL.formatted(myClass.getConstructor(myClass.getName()).getLabel())
        );
    }

    public static void store_ref(String label) {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            label
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(), "1",
            Comment.ATTRIBUTE_STORE.formatted(myClass.getName())
        );
    }

    public static void generate() {
        SymbolTable.getGenerator().write(CodegenConfig.DATA, Comment.STRING_SECTION);
        strings.forEach((string, label) -> {
            SymbolTable.getGenerator().write(
                Labeler.getLabel(CodegenConfig.LABEL, label),
                Instruction.DW.toString(),
                string, ", 0"
            );
        });
    }
}
