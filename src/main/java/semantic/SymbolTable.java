package main.java.semantic;

import main.java.model.Error;
import main.java.model.ErrorType;
import main.java.model.Pair;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.Constructor;
import main.java.config.SemanticConfig;
import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class SymbolTable {
    public static Class actualClass;
    public static Unit actualUnit;
    public static Map<Integer, Pair<List<Error>, String>> errors;
    private final static Map<String, Class> classes = new HashMap<>();
    private static boolean hasMain = false;
    private static Token EOF;

    public static void init(Map<Integer, Pair<List<Error>, String>> errors_map) {
        errors = errors_map;
        classes.clear();
        resetActualClass();
        resetActualUnit();
        addPrimitiveClasses();
        hasMain = false;
        EOF = null;
    }

    public static void validate() throws SemanticException {
        for (Class myClass:classes.values()) {
            actualClass = myClass;
            myClass.validate();
        }
        if (!hasMain) throwException(SemanticErrorMessages.MAIN_NOT_FOUND, EOF);
    }

    public static void consolidate() {
        if (errors.isEmpty()) for (Class myClass:classes.values()) {
            actualClass = myClass;
            myClass.consolidate();
        }
    }

    public static void check() throws SemanticException {
        if (errors.isEmpty()) for (Class myClass:classes.values()) {
            actualClass = myClass;
            myClass.check();
        }
    }

    public static void foundMain() {
        hasMain = true;
    }

    public static boolean hasClass(String class_name) {
        return classes.containsKey(class_name);
    }

    public static Class getClass(String class_name) {
        return classes.get(class_name);
    }

    public static void addClass(Class newClass) {
        if (newClass == null) return;
        if (classes.containsKey(newClass.getName())) saveError(
            String.format(
                SemanticErrorMessages.CLASS_DUPLICATE,
                newClass.getName()
            ),
            newClass.getToken()
        );
        else classes.put(newClass.getName(), newClass);
    }

    public static void resetActualClass() {
        actualClass = null;
    }

    public static void resetActualUnit() {
        actualUnit = null;
    }

    public static void saveEOF(Token EOF) {
        SymbolTable.EOF = EOF;
    }

    public static Constructor getNewDefaultConstructor() {
        return new Constructor(
            actualClass.getName(),
            new Token(
                TokenType.idClass,
                actualClass.getName(),
                0,
                0
            )
        );
    }

// --------------------------------------------------------------------------------------------------------------------

    private static void addPrimitiveClasses() {
        addClass(main.java.semantic.entities.predefined.Object.Class());
        addClass(main.java.semantic.entities.predefined.String.Class());
        addClass(main.java.semantic.entities.predefined.System.Class());
    }

    public static void saveError(String message, Token token) {
        if (token == null) return;

        if (!errors.containsKey(token.getLine()))
            errors.put(token.getLine(), new Pair<>(new ArrayList<>(), ""));

        errors.get(token.getLine()).getFirst().add(new Error(
            message,
            token.getLexeme(),
            token.getLine(),
            token.getColumn(),
            ErrorType.Semantic
        ));
    }

    public static void throwException(String message, Token token) throws SemanticException {
        saveError(message, token);
        if (!SemanticConfig.CONTINUE_ON_ERROR) throw new SemanticException(message);
    }
}
