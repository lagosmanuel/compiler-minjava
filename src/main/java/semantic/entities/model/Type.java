package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.model.TokenType;

import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.type.ClassType;
import main.java.semantic.entities.model.type.PrimitiveType;
import main.java.semantic.entities.model.type.TypeVar;
import main.java.semantic.entities.model.type.VarType;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public abstract class Type extends Entity {
    protected final List<TypeVar> type_params = new ArrayList<>();

    public Type(String type_name, Token type_token, List<TypeVar> type_params) {
        super(type_name, type_token);
        if (type_params != null) this.type_params.addAll(type_params);
    }

    public Type(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    public abstract boolean compatible(Type type);

    public boolean isBoolean() {
        return Objects.equals(getName(), PrimitiveType.BOOLEAN) ||
               Objects.equals(getName(), ClassType.BOOLEAN_WRAPPER);
    }

    public boolean isInt() {
        return Objects.equals(getName(), PrimitiveType.INT) ||
               Objects.equals(getName(), ClassType.INT_WRAPPER);
    }

    public boolean isFloat() {
        return Objects.equals(getName(), PrimitiveType.FLOAT) ||
               Objects.equals(getName(), ClassType.FLOAT_WRAPPER);
    }

    public boolean isChar() {
        return Objects.equals(getName(), PrimitiveType.CHAR) ||
               Objects.equals(getName(), ClassType.CHAR_WRAPPER);
    }

    public boolean isNumeric() {
        return isInt() || isFloat();
    }

    public TypeVar getTypeParam(int position) {
        if (position < 0 || position >= type_params.size()) return null;
        return type_params.get(position);
    }

    public List<TypeVar> getTypeParams() {
        return type_params;
    }

    public int getTypeParamsCount() {
        return type_params.size();
    }

    public boolean compare(Type type) {
        if (type == this) return true;
        if (type == null) return false;

        if (type instanceof TypeVar typeVar) {
            if (typeVar.getInstaceType() == null) {
                return Objects.equals(
                    SymbolTable.actualClass.getSuperType().getTypeParam(typeVar.getPosition()).getName(),
                    getName()
                );
            } else {
                return this.compare(typeVar.getInstaceType());
            }
        }

        if (this instanceof TypeVar thisTypeVar) {
            if (thisTypeVar.getInstaceType() == null) {
                return false;
            } else {
                return Objects.equals(thisTypeVar.getInstaceType().getName(), type.getName());
            }
        }

        if (type.getClass() != this.getClass()) return false;
        return Objects.equals(this.getName(), type.getName()) &&
               typeParamsMatch(type);
    }

    private boolean typeParamsMatch(Type type) {
        boolean match = getTypeParamsCount() == type.getTypeParamsCount();
        for (int i = 0; i < getTypeParamsCount() && match; ++i)
            match = getTypeParam(i).compare(type.getTypeParam(i));
        return match;
    }

    public static Type createType(Token type_token, List<TypeVar> type_params_tokens) {
        if (type_token == null) return null;

        if (type_token.getType() != TokenType.idClass) {
            if (type_token.getType() == TokenType.kwVar) {
                return new VarType(
                    type_token.getLexeme(),
                    type_token
                );
            } else return new PrimitiveType(
                type_token.getLexeme(),
                type_token
            );
        } else if (SymbolTable.actualClass != null && SymbolTable.actualClass.hasTypeParameter(type_token.getLexeme())) {
            return new TypeVar(
                type_token.getLexeme(),
                type_token,
                type_params_tokens,
                SymbolTable.actualClass.getTypeParameter(type_token.getLexeme()).getPosition()
            );
        } else {
            return new ClassType(
                type_token.getLexeme(),
                type_token,
                type_params_tokens
            );
        }
    }

    public static Type createType(TokenType type_token) {
        return switch (type_token) {
            case intLiteral -> PrimitiveType.INT_TYPE;
            case floatLiteral -> PrimitiveType.FLOAT_TYPE;
            case charLiteral -> PrimitiveType.CHAR_TYPE;
            case falseLiteral, trueLiteral -> PrimitiveType.BOOLEAN_TYPE;
            case nullLiteral -> ClassType.NULL_TYPE;
            case stringLiteral -> ClassType.STRING_TYPE;
            default -> null;
        };
    }

    public Type convertNumeric() {
        return isChar()? PrimitiveType.INT_TYPE:this;
    }
}
