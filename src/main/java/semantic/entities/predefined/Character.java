package main.java.semantic.entities.predefined;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.*;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.statement.Assignment;
import main.java.semantic.entities.model.statement.Block;
import main.java.semantic.entities.model.statement.ExpressionStatement;
import main.java.semantic.entities.model.statement.Return;
import main.java.semantic.entities.model.statement.primary.VarAccess;
import main.java.semantic.entities.model.type.PrimitiveType;

public class Character {
    private static boolean initialized = false;
    public static final java.lang.String name = "Character";

    public static final Token token = new Token(
        TokenType.idClass,
        name,
        0,
        0
    );

    private static final Class object = new Class(name, token);

    public static Class Class() {
        if (!initialized) init();
        return object;
    }

    private static void init() {
        initialized = true;
        Class actualClass = SymbolTable.actualClass;
        SymbolTable.actualClass = object;
        addAttributes();
        addConstructor();
        addMethods();
        SymbolTable.actualClass = actualClass;
    }

    private static void addAttributes() {
        object.addAttribute(new Attribute(
            "value",
            new Token(TokenType.idMetVar, "value", 0, 0),
            PrimitiveType.CHAR_TYPE,
            false,
            false
        ));
    }

    private static void addConstructor() {
        Constructor constructor = new Constructor(name + "@X", token);
        constructor.addParameter(new Parameter(
            "init",
            new Token(TokenType.idMetVar, "init", 0, 0),
            PrimitiveType.CHAR_TYPE
        ));
        Block code = new Block(new Token(TokenType.leftBrace, "{", 0, 0));
        code.addStatement(new ExpressionStatement(new Assignment(
                new VarAccess(new Token(TokenType.idMetVar, "value", 0, 0)),
                new VarAccess(new Token(TokenType.idMetVar, "init", 0, 0)),
                new Token(TokenType.opAssign, "=", 0, 0)
        )));
        constructor.setBody(code);
        object.addConstructor(constructor);
    }

    public static void addMethods() {
        Method method = new Method(
            "charValue",
            new Token(TokenType.idMetVar, "charValue", 0, 0)
        );
        method.setReturnType(PrimitiveType.CHAR_TYPE);
        Block code = new Block(new Token(TokenType.leftBrace, "{", 0, 0));
        Return ret = new Return(
                new Token(TokenType.kwReturn, "return", 0, 0),
                new VarAccess(new Token(TokenType.idMetVar, "value", 0, 0))
        );
        code.addStatement(ret);
        method.setBody(code);
        object.addMethod(method);
    }
}
