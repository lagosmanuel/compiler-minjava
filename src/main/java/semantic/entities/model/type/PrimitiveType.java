package main.java.semantic.entities.model.type;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.Objects;
import java.util.Set;

public class PrimitiveType extends Type {
    public static final String INT = "int";
    public static final String FLOAT = "float";
    public static final String BOOLEAN = "boolean";
    public static final String CHAR = "char";
    public static final String VOID = "void";

    public static final Set<String> types = Set.of(
        INT, FLOAT, BOOLEAN, CHAR, VOID
    );

    public PrimitiveType(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();
        if (!types.contains(getName())) SymbolTable.throwException(
            String.format(
                SemanticErrorMessages.TYPE_NOTFOUND,
                getName()
            ),
            getToken()
        );
    }

    @Override
    public boolean compatible(Type type) {
        if (type == null) return false;
        if (type == this) return true;

        if (Objects.equals(this.getName(), type.getName())) return true;

        if (Objects.equals(this.getName(), INT))
            return Objects.equals(type.getName(), ClassType.INT_WRAPPER) || type.isChar();
        if (Objects.equals(this.getName(), FLOAT))
            return Objects.equals(type.getName(), ClassType.FLOAT_WRAPPER) || type.isInt();
        if (Objects.equals(this.getName(), CHAR))
            return Objects.equals(type.getName(), ClassType.CHAR_WRAPPER);
        if (Objects.equals(this.getName(), BOOLEAN))
            return Objects.equals(type.getName(), ClassType.BOOLEAN_WRAPPER);

        return false;
    }

    public static final Type INT_TYPE = new PrimitiveType(
        PrimitiveType.INT,
        new Token(TokenType.kwInt, PrimitiveType.INT, 0, 0)
    );

    public static final Type FLOAT_TYPE = new PrimitiveType(
        PrimitiveType.FLOAT,
        new Token(TokenType.kwFloat, PrimitiveType.FLOAT, 0, 0)
    );

    public static final Type BOOLEAN_TYPE = new PrimitiveType(
        PrimitiveType.BOOLEAN,
        new Token(TokenType.kwBoolean, PrimitiveType.BOOLEAN, 0, 0)
    );

    public static final Type CHAR_TYPE = new PrimitiveType(
        PrimitiveType.CHAR,
        new Token(TokenType.kwChar, PrimitiveType.CHAR, 0, 0)
    );
}
