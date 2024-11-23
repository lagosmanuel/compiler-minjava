package main.java.symboltable.entities.predefined;

import main.java.config.SemanticConfig;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.Class;
import main.java.symboltable.entities.Method;
import main.java.symboltable.entities.Parameter;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.type.ClassType;
import main.java.symboltable.entities.type.PrimitiveType;

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
        Class actualClass = SymbolTable.actualClass;
        SymbolTable.actualClass = object;
        addDebugPrint();
        SymbolTable.actualClass = actualClass;
    }

    private static void addDebugPrint() {
        Method method = new Method(
            "debugPrint%sX".formatted(SemanticConfig.PARAMETER_TYPE_SEPARATOR),
            new Token(
                TokenType.idMetVar,
                "debugPrint",
                0,
                0
            )
        );
        method.setStatic();
        method.setReturnType(new PrimitiveType("void", new Token(
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
        method.setBody(new BlockPredefined(MethodPredefined.debugPrint));
        object.addMethod(method);
    }
}
