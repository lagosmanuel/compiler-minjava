package main.java.symboltable.entities;

import main.java.model.Token;
import main.java.symboltable.entities.type.Type;

public class Parameter extends Variable {
    public Parameter(String param_name, Token param_token, Type param_type) {
        super(param_name, param_token, param_type);
    }
}
