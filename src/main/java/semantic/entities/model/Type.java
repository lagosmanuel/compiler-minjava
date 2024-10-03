package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.model.TokenType;

import main.java.semantic.entities.model.type.ClassType;
import main.java.semantic.entities.model.type.PrimitiveType;
import main.java.semantic.entities.model.type.TypeVar;

import java.util.List;
import java.util.ArrayList;

public abstract class Type extends Entity {
    protected final List<TypeVar> type_params = new ArrayList<>();

    public Type(String type_name, Token type_token, List<TypeVar> type_params) {
        super(type_name, type_token);
        if (type_params != null) this.type_params.addAll(type_params);
    }

    public Type(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    public List<TypeVar> getTypeParams() {
        return type_params;
    }

    public int getTypeParamsCount() {
        return type_params.size();
    }

    public static Type createType(Token type_token, List<TypeVar> type_params_tokens) {
        if (type_token == null) return null;
        return type_token.getType() != TokenType.idClass?
            new PrimitiveType(type_token.getLexeme(), type_token):
            new ClassType(type_token.getLexeme(), type_token, type_params_tokens);
    }
}
