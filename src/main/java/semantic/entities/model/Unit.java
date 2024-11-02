package main.java.semantic.entities.model;

import main.java.model.Token;
import main.java.config.SemanticConfig;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Parameter;
import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;
import main.java.semantic.entities.model.statement.Block;
import main.java.semantic.entities.model.statement.expression.Expression;
import main.java.semantic.entities.model.type.PrimitiveType;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public abstract class Unit extends Entity {
    protected Type return_type;
    protected final Map<String, Parameter> parameters;
    protected final List<Parameter> parameter_list;
    protected Block body;

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

    public void unsetStatic() {
        this.is_static = false;
    }

    public boolean isPrivate() {
        return is_private;
    }

    public void setPrivate() {
        this.is_private = true;
    }

    public Type getReturnType() {
        return return_type;
    }

    public void setReturnType(Type return_type) {
        this.return_type = return_type;
    }

    public boolean hasParameter(String param_name) {
        return parameters.containsKey(param_name);
    }

    public Parameter getParameter(String param_name) {
        return parameters.get(param_name);
    }

    public Parameter getParameter(int index) {
        return parameter_list.get(index);
    }

    public int getParameterCount() {
        return parameters.size();
    }

    public void addParameter(Parameter parameter) {
        if (parameter == null) return;
        if (parameters.containsKey(parameter.getName())) {
            SymbolTable.saveError(
                String.format(
                    SemanticErrorMessages.PARAMETER_DUPLICATE,
                    parameter.getName()
                ),
                parameter.getToken()
            );
        } else {
            parameters.put(parameter.getName(), parameter);
            parameter_list.add(parameter);
        }
    }

    public void setBody(Block body) {
        this.body = body;
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();
        for (Parameter parameter:parameters.values())
            parameter.validate();
        if (return_type != null) return_type.validate();
    }

    public void check() throws SemanticException {
        if (body == null || body.checked()) return;
        SymbolTable.actualUnit = this;
        body.check();
        if (!body.hasReturn() && getReturnType() != null && !Objects.equals(getReturnType().getName(), PrimitiveType.VOID)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.UNIT_NO_RETURN,
                    getReturnType().getName()
                ),
                getToken()
            );
        }
    }

    public boolean isCompatible(Unit unit) {
        return Objects.equals(name, unit.name) &&
               returnMatch(unit) &&
               is_static == unit.is_static &&
               is_private == unit.is_private &&
               parametersMatch(unit);
    }

    private boolean returnMatch(Unit unit) {
        if (return_type == null) return unit.return_type == null;
        return return_type.compare(unit.return_type);
    }

    private boolean parametersMatch(Unit unit) {
        boolean match = parameter_list.size() == unit.parameter_list.size();
        for (int i = 0; i < parameter_list.size() && match; ++i)
            match = parameter_list.get(i).getType().compare(unit.parameter_list.get(i).getType());
        return match;
    }

    public boolean argumentsMatch(List<Expression> arguments, Token error) throws SemanticException {
        if (arguments == null) return false;
        if (arguments.size() != getParameterCount()) return false;
        boolean compatible = true;
        for (int i = 0; i < arguments.size(); ++i) {
            Type argumentType = arguments.get(i).checkType();
            Type paramType = getParameter(i).getType();
            if (argumentType != null && paramType != null && !paramType.compatible(argumentType)) {
                compatible = false;
                SymbolTable.throwException(
                    String.format(
                        SemanticErrorMessages.ARGUMENT_TYPE_NOT_COMPATIBLE,
                        i+1,
                        getToken().getLexeme(),
                        paramType.getName(),
                        argumentType.getName()
                    ),
                    error
                );
            }
        }
        return compatible;
    }

    public static String getMangledName(String name, int arguments) {
        String separator = arguments > 0? SemanticConfig.PARAMETER_TYPE_SEPARATOR:"";
        String counter = "X".repeat(arguments);
        return name + separator + counter;
    }
}
