package main.java.semantic.entities.predefined;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.Method;
import main.java.semantic.entities.Parameter;
import main.java.semantic.entities.model.type.ClassType;
import main.java.semantic.entities.model.type.PrimitiveType;

public class System {
    private static boolean initialized = false;
    public static java.lang.String name = "System";

    public static Token token = new Token(
        TokenType.idClass,
        name,
        0,
        0
    );

    private static final Class system = new Class(name, token);

    public static Class Class() {
        if (!initialized) init();
        return system;
    }

    private static void init() {
        addMethodRead();
        addMethodPrintB();
        addMethodPrintC();
        addMethodPrintI();
        addMethodPrintS();
        addMethodPrintln();
        addMethodPrintBln();
        addMethodPrintCln();
        addMethodPrintIln();
        addMethodPrintSln();
        initialized = true;
    }

    private static void addMethodPrintSln() {
        Method method = new Method(
            "printSln@X",
            new Token(
                TokenType.idMetVar,
                "printSln",
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
        method.addParameter("s", new Parameter(
            "s",
            new Token(
                TokenType.idMetVar,
                "s",
                0,
                0
            ),
            new ClassType("String", new Token(
                TokenType.idMetVar,
                "String",
                0,
                0
            ))
        ));
        system.addMethod("printSln@X", method);
    }

    private static void addMethodPrintIln() {
        Method method = new Method(
            "printIln@X",
            new Token(
                TokenType.idMetVar,
                "printIln",
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
        method.addParameter("i", new Parameter(
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
        system.addMethod("printIln@X", method);
    }

    private static void addMethodPrintCln() {
        Method method = new Method(
            "printCln@X",
            new Token(
                TokenType.idMetVar,
                "printCln",
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
        method.addParameter("c", new Parameter(
            "c",
            new Token(
                TokenType.idMetVar,
                "c",
                0,
                0
            ),
            new PrimitiveType("char", new Token(
                TokenType.idMetVar,
                "char",
                0,
                0
            ))
        ));
        system.addMethod("printCln@X", method);
    }

    private static void addMethodPrintBln() {
        Method method = new Method(
            "printBln@X",
            new Token(
                TokenType.idMetVar,
                "printBln",
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
        method.addParameter("b", new Parameter(
            "b",
            new Token(
                TokenType.idMetVar,
                "b",
                0,
                0
            ),
            new PrimitiveType("boolean", new Token(
                TokenType.idMetVar,
                "boolean",
                0,
                0
            ))
        ));
        system.addMethod("printBln@X", method);
    }

    private static void addMethodPrintln() {
        Method read = new Method(
            "println",
            new Token(
                TokenType.idMetVar,
                "println",
                0,
                0
            )
        );
        read.setStatic();
        read.setReturn(new PrimitiveType("void", new Token(
            TokenType.idMetVar,
            "void",
            0,
            0
        )));
        system.addMethod("println", read);
    }

    private static void addMethodPrintS() {
        Method method = new Method(
            "printS@X",
            new Token(
                TokenType.idMetVar,
                "printS",
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
        method.addParameter("s", new Parameter(
            "s",
            new Token(
                TokenType.idMetVar,
                "s",
                0,
                0
            ),
            new ClassType("String", new Token(
                TokenType.idMetVar,
                "String",
                0,
                0
            ))
        ));
        system.addMethod("printS@X", method);
    }

    private static void addMethodPrintI() {
        Method method = new Method(
            "printI@X",
            new Token(
                TokenType.idMetVar,
                "printI",
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
        method.addParameter("i", new Parameter(
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
        system.addMethod("printI@X", method);
    }

    private static void addMethodPrintC() {
        Method method = new Method(
            "printC@X",
            new Token(
                TokenType.idMetVar,
                "printC",
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
        method.addParameter("c", new Parameter(
            "c",
            new Token(
                TokenType.idMetVar,
                "c",
                0,
                0
            ),
            new PrimitiveType("char", new Token(
                TokenType.idMetVar,
                "char",
                0,
                0
            ))
        ));
        system.addMethod("printC@X", method);
    }

    private static void addMethodPrintB() {
        Method method = new Method(
            "printB@X",
            new Token(
                TokenType.idMetVar,
                "printB",
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
        method.addParameter("b", new Parameter(
            "b",
            new Token(
                TokenType.idMetVar,
                "b",
                0,
                0
            ),
            new PrimitiveType("boolean", new Token(
                TokenType.idMetVar,
                "boolean",
                0,
                0
            ))
        ));
        system.addMethod("printB@X", method);
    }

    private static void addMethodRead() {
        Method method = new Method(
            "read",
            new Token(
                TokenType.idMetVar,
                "read",
                0,
                0
            )
        );
        method.setStatic();
        method.setReturn(new PrimitiveType("int", new Token(
            TokenType.idMetVar,
            "int",
            0,
            0
        )));
        system.addMethod("read", method);
    }
}
