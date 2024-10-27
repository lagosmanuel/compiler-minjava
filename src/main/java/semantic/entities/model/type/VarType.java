package main.java.semantic.entities.model.type;

import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.entities.model.Type;

public class VarType extends Type {
    public VarType(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    @Override
    public boolean compatible(Type type) {
        return type != null &&
               type.getToken().getType() != TokenType.nullLiteral &&
               type.getToken().getType() != TokenType.kwVoid;
    }
}