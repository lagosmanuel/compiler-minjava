package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Parameter;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public abstract class Unit extends Entity {
    protected Type return_type;
    protected final Map<String, Parameter> parameters;
    protected final List<Parameter> parameter_list;

    protected boolean is_private = false;
    protected boolean is_static = false;

    public Unit(String unit_name, Token unit_token) {
        super(unit_name, unit_token);
        this.parameters = new HashMap<>();
        this.parameter_list = new ArrayList<>();
    }

    public boolean isStatic() {
        return is_static;
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

    public void setReturn(Type return_type) {
        this.return_type = return_type;
    }

    public void addParameter(String param_name, Parameter parameter) {
        if (parameters.containsKey(param_name)) {
            SymbolTable.saveError(
                String.format(
                    SemanticErrorMessages.PARAMETER_DUPLICATE,
                    param_name
                ),
                parameter.getToken()
            );
        } else {
            parameters.put(param_name, parameter);
            parameter_list.add(parameter);
        }
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();
        for (Parameter parameter:parameters.values())
            parameter.validate();
        if (return_type != null) return_type.validate();
    }

    public boolean isCompatible(Unit unit) {
        return Objects.equals(name, unit.name) &&
               Objects.equals(return_type, unit.return_type) &&
               is_static == unit.is_static &&
               is_private == unit.is_private &&
               parametersMatch(unit);
    }

    public boolean parametersMatch(Unit unit) {
        boolean match = parameter_list.size() == unit.parameter_list.size();
        for (int i = 0; i < parameter_list.size() && match; ++i)
            match = Objects.equals(
                parameter_list.get(i).getType(),
                unit.parameter_list.get(i).getType()
            );
        return match;
    }
}