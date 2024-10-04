package main.java.semantic.entities.predefined;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.Method;
import main.java.semantic.entities.Parameter;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.type.ClassType;
import main.java.semantic.entities.model.type.PrimitiveType;

public class Object {
    private static boolean initialized = false;
    public static final java.lang.String name = "Object";

    public static final Token token = new Token(
        TokenType.idClass,
        name,
        0,
        0
    );

    public static final Type type = new ClassType(name, token, null);

    private static final Class object = new Class(name, token);

    public static Class Class() {
        if (!initialized) init();
        return object;
    }

    private static void init() {
        initialized = true;
        addDebugPrint();
    }

    private static void addDebugPrint() {
        Method method = new Method(
            "debugPrint@X",
            new Token(
                TokenType.idMetVar,
                "debugPrint",
                0,
                0
            )
        );

        method.setStatic();

        method.setReturn(new PrimitiveType("void", new Token(
            TokenType.idMetVar,
            "void",
            0,
            0
        )));

        method.addParameter(new Parameter(
            "i",
            new Token(
                TokenType.idMetVar,
                "i",
                0,
                0
            ),
            new PrimitiveType("int", new Token(
                TokenType.idMetVar,
                "int",
                0,
                0
            ))
        ));

        object.addMethod("debugPrint@X", method);
    }
}
