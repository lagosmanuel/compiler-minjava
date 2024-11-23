package main.java.symboltable.entities.type;

import main.java.model.Token;
import main.java.model.TokenType;

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