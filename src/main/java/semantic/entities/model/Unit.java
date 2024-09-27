package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Parameter;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Unit extends Entity {
    protected Type return_type;
    protected final Map<String, Parameter> parameters;

    protected boolean is_private = false;
    protected boolean is_static = false;

    public Unit(String unit_name, Token unit_token) {
        super(unit_name, unit_token);
        this.parameters = new HashMap<>();
    }

    public void setStatic() {
        this.is_static = true;
    }

    public boolean isPrivate() {
        return is_private;
    }

    public void setPrivate() {
        this.is_private = true;
    }

    public Type getReturn() {
        return return_type;
    }

    public void setReturn(Token return_type_token) {
        this.return_type = Type.createType(return_type_token);
    }

    public void addParameter(String param_name, Token param_token, Token type_token) {
        if (parameters.containsKey(param_name))
            SymbolTable.saveError(SemanticErrorMessages.PARAMETER_REDECLARATION, param_token);
        else parameters.put(param_name, new Parameter(param_name, param_token, type_token));
    }

    @Override
    public void validate() throws SemanticException {
        super.validate();
        for (Parameter parameter : parameters.values())
            parameter.validate();
        if (return_type != null) return_type.validate();
    }

    public boolean isCompatible(Unit unit) {
        return Objects.equals(name, unit.name) &&
               Objects.equals(return_type.getName(), unit.return_type.getName()) &&
               is_static == unit.is_static &&
               is_private == unit.is_private;
    }
}
