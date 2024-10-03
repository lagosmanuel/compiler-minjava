package main.java.semantic.entities.model.type;

import main.java.model.Token;
import main.java.semantic.entities.model.Type;

public class TypeVar extends ClassType {
    Type instanceType;

    public TypeVar(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    public Type getInstaceType() {
        return instanceType;
    }
}
