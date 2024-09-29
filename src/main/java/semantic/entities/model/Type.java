package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.model.TokenType;

import main.java.semantic.entities.model.type.ClassType;
import main.java.semantic.entities.model.type.PrimitiveType;

import java.util.List;
import java.util.ArrayList;

public abstract class Type extends Entity {
    protected List<Token> type_params_tokens = new ArrayList<>();

    public Type(String type_name, Token type_token, List<Token> type_params_tokens) {
        super(type_name, type_token);
        if (type_params_tokens != null) this.type_params_tokens.addAll(type_params_tokens);
    }

    public List<Token> getTypeParams() {
        return type_params_tokens;
    }

    public static Type createType(Token type_token, List<Token> type_params_tokens) {
        if (type_token == null) return null;
        return type_token.getType() != TokenType.idClass?
            new PrimitiveType(type_token.getLexeme(), type_token, type_params_tokens):
            new ClassType(type_token.getLexeme(), type_token, type_params_tokens);
    }
}
