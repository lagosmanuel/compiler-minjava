package main.java.semantic.entities.model.type;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.predefined.Object;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;
import java.util.Objects;

public class ClassType extends Type {
    public static final String INT_WRAPPER = "Integer";
    public static final String FLOAT_WRAPPER = "Float";
    public static final String BOOLEAN_WRAPPER = "Boolean";
    public static final String CHAR_WRAPPER = "Character";
    public static final String STRING = "String";
    public static final String NULL = "null";

    public static final Type STRING_TYPE = new ClassType(
        ClassType.STRING,
        new Token(TokenType.idClass, ClassType.STRING, 0, 0)
    );

    public static final Type NULL_TYPE = new ClassType(
        ClassType.NULL,
        new Token(TokenType.nullLiteral, ClassType.NULL, 0, 0)
    );

    public ClassType(String class_name, Token class_token, List<TypeVar> type_params) {
        super(class_name, class_token, type_params);
    }

    public ClassType(String class_name, Token class_token) {
        super(class_name, class_token);
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();

        if (!SymbolTable.hasClass(getName()) && !SymbolTable.actualClass.hasTypeParameter(getName()))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.CLASS_NOT_DECLARED,
                    getName()
                ),
                getToken()
            );

        if (SymbolTable.actualClass.hasTypeParameter(getName())) {
            if (getTypeParamsCount() > 0)
                SymbolTable.throwException(SemanticErrorMessages.TYPE_PARAMETER_RECURSIVE, getToken());
        } else if (SymbolTable.hasClass(getName())) {
            if (SymbolTable.getClass(getName()).getTypeParametersCount() != getTypeParamsCount())
                SymbolTable.throwException(
                    String.format(
                        SemanticErrorMessages.INVALID_TYPE_PARAMETERS_COUNT,
                        SymbolTable.getClass(getName()).getTypeParametersCount()
                    ),
                    getToken()
                );
            for (TypeVar typeVar:getTypeParams()) typeVar.validate();
        }
    }

    @Override
    public boolean compatible(Type type) {
        if (type == null) return false;
        if (type == this) return true;
        if (Objects.equals(this.getName(), Object.name)) return true;
        if (Objects.equals(type.getName(), NULL)) return true;

        if (Objects.equals(this.getName(), type.getName()) ||
            (type instanceof ClassType classType &&
            !Objects.equals(classType.getName(), Object.name) &&
            this.compatible(classType.getSuperType()))
        ) return true;

        if (Objects.equals(this.getName(), INT_WRAPPER)) return Objects.equals(type.getName(), PrimitiveType.INT) || type.isChar();
        if (Objects.equals(this.getName(), FLOAT_WRAPPER)) return Objects.equals(type.getName(), PrimitiveType.FLOAT) || type.isInt();
        if (Objects.equals(this.getName(), CHAR_WRAPPER)) return Objects.equals(type.getName(), PrimitiveType.CHAR);
        if (Objects.equals(this.getName(), BOOLEAN_WRAPPER)) return Objects.equals(type.getName(), PrimitiveType.BOOLEAN);
        if (Objects.equals(this.getName(), STRING)) return type.isChar() || type.isInt();

        return false;
    }

    public Type getSuperType() {
        return SymbolTable.hasClass(getName())?
            SymbolTable.getClass(getName()).getSuperType():null;
    }
}
