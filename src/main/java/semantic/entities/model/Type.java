package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.model.TokenType;

import main.java.semantic.entities.model.type.ClassType;
import main.java.semantic.entities.model.type.PrimitiveType;

public abstract class Type extends Entity {
    public Type(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    public static Type createType(Token type_token) {
        if (type_token == null) return null;
        return type_token.getType() != TokenType.idClass?
            new PrimitiveType(type_token.getLexeme(), type_token):
            new ClassType(type_token.getLexeme(), type_token);
    }
}
