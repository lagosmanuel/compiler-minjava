package main.java.semantic.entities.model.type;

import main.java.model.Token;
import main.java.semantic.entities.model.Type;

public class TypeVar extends ClassType {
    Type instanceType;

    public TypeVar(String type_name, Token type_token) {
        super(type_name, type_token);
    }

    public boolean hasInstanceType() {
        return instanceType != null;
    }

    public Type getInstaceType() {
        return instanceType;
    }

    public void setInstanceType(Type instanceType) {
        this.instanceType = instanceType;
    }
}
