package main.java.semantic.entities.predefined;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.type.ClassType;
import main.java.semantic.entities.model.type.PrimitiveType;
import main.java.semantic.entities.model.type.TypeVar;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.AbstractMethod;

import java.util.List;

public class MiniIterable {
    private static boolean initialized = false;
    public static final java.lang.String name = "MiniIterable";

    public static final Token token = new Token(
        TokenType.idClass,
        name,
        0,
        0
    );

    public static final TypeVar T = new TypeVar(
        "T",
        new Token(
            TokenType.idClass,
            "T",
            0,
            0
        ),
        null,
        0
    );

    public static final Type type = new ClassType(name, token, List.of(T));
    private static final Class object = new Class(name, token);

    public static Class Class() {
        if (!initialized) init();
        return object;
    }

    private static void init() {
        initialized = true;
        Class actualClass = SymbolTable.actualClass;
        SymbolTable.actualClass = object;
        object.setAbstract();
        addTypeVar();
        addStart();
        addNext();
        addHasNext();
        SymbolTable.actualClass = actualClass;
    }

    private static void addTypeVar() {
        object.addTypeParameter(T);
    }

    private static void addStart() {
        AbstractMethod method = new AbstractMethod(
            "start",
            new Token(
                TokenType.idMetVar,
                "start",
                0,
                0
            )
        );
        method.setReturnType(PrimitiveType.VOID_TYPE);
        object.addAbstractMethod(method);
    }

    private static void addNext() {
        AbstractMethod method = new AbstractMethod(
            "next",
            new Token(
                TokenType.idMetVar,
                "next",
                0,
                0
            )
        );
        method.setReturnType(T);
        object.addAbstractMethod(method);
    }

    private static void addHasNext() {
        AbstractMethod method = new AbstractMethod(
            "hasNext",
            new Token(
                    TokenType.idMetVar,
                    "hasNext",
                    0,
                    0
            )
        );
        method.setReturnType(PrimitiveType.BOOLEAN_TYPE);
        object.addAbstractMethod(method);
    }
}
