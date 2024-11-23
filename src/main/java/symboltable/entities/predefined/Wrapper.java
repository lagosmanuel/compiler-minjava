package main.java.symboltable.entities.predefined;

import main.java.codegen.Comment;
import main.java.codegen.Instruction;
import main.java.config.CodegenConfig;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.Class;
import main.java.symboltable.entities.Constructor;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.Unit;

import java.lang.String;

public class Wrapper {
    public static void unwrap(Type type) {
        if (type == null) return;
        switch (type.getName()) {
            case Integer.name, Float.name, Boolean.name, Character.name -> {
                SymbolTable.getGenerator().write(
                    Instruction.LOADREF.toString(), "1",
                    Comment.ATTRIBUTE_LOAD.formatted(type.getName())
                );
            }
        }
    }

    public static void wrap(Type type) {
        if (type == null) return;
        switch (type.getName()) {
            case Integer.name, Float.name, Boolean.name, Character.name -> {
                Class myclass = SymbolTable.getClass(type.getName());
                allocate_result();
                malloc_call();
                store_vt_cir(myclass.getName(), myclass.getVTLabel());
                save_this_ref();
                call_constructor(myclass.getConstructor(Unit.getMangledName(myclass.getName(), 1)));
            }
        }
    }

    private static void allocate_result() {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString(),
            Comment.CONSTRUCTOR_ALLOC
        );
    }

    private static void malloc_call() {
        SymbolTable.getGenerator().write(
            Instruction.RMEM.toString(), "1",
            Comment.RETURN_ALLOC.formatted(CodegenConfig.MALLOC_LABEL)
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(), "2",
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

    private static void store_vt_cir(String classname, String vt_label) {
        SymbolTable.getGenerator().write(
            Instruction.DUP.toString()
        );
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(), vt_label,
            Comment.VT_LOAD.formatted(classname)
        );
        SymbolTable.getGenerator().write(
            Instruction.STOREREF.toString(), "0",
            Comment.VT_STORE.formatted(classname)
        );
    }

    private static void save_this_ref() {
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
            java.lang.String.valueOf(4),
            Comment.CONSTRUCTOR_SAVE_THIS
        );
    }

    private static void call_constructor(Constructor constructor) {
        SymbolTable.getGenerator().write(
            Instruction.PUSH.toString(),
            constructor.getLabel(),
            Comment.CONSTRUCTOR_LOAD.formatted(constructor.getLabel())
        );
        SymbolTable.getGenerator().write(
            Instruction.CALL.toString(),
            Comment.CONSTRUCTOR_CALL.formatted(constructor.getLabel())
        );
    }
}