package main.java.semantic.entities;

import main.java.model.Token;

import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Entity;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.predefined.Object;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import main.java.messages.SemanticErrorMessages;
import main.java.exeptions.SemanticException;

public class Class extends Entity {
    protected final Map<String, Method> methods = new HashMap<>();
    protected final Map<String, Constructor> constructors = new HashMap<>();
    protected final Map<String, AbstractMethod> abstractMethods = new HashMap<>();
    protected final Map<String, List<Attribute>> attributes = new HashMap<>();
    protected final Map<String, Token> type_parameters = new HashMap<>();

    protected final List<Attribute> instance_attributes = new ArrayList<>();
    protected final List<Attribute> class_attributes = new ArrayList<>();
    protected final List<Method> static_methods_list = new ArrayList<>();
    protected final List<Method> dynamic_methods_list = new ArrayList<>();

    protected Type super_type = Object.type;
    protected boolean is_abstract;
    protected boolean is_consolidated;

    public Class(String class_name, Token class_token, List<Token> type_params_tokens) {
        super(class_name, class_token);
        type_params_tokens.forEach(this::addTypeParameter);
    }

    public Class(String class_name, Token class_token) {
        super(class_name, class_token);
    }

    @Override
    public void validate() throws SemanticException {
        if (isValidated()) return;
        super.validate();

        superNotDeclared();
        cyclicInheritance(Stream.of(getName()).collect(Collectors.toSet()));

        for (Constructor constructor:constructors.values()) constructor.validate();
        for (Method method:methods.values()) method.validate();
        for (AbstractMethod abstractMethod:abstractMethods.values()) abstractMethod.validate();
        for (List<Attribute> attribute_list:attributes.values()) attribute_list.getFirst().validate();

        consolidate();
    }

    public void consolidate() throws SemanticException {
        if (Objects.equals(name, Object.name)) return;
        if (!isValidated()) validate();
        if (is_consolidated) return;

        Class superClass = SymbolTable.getClass(super_type.getName());
        superClass = superClass == null? Object.Class():superClass; // TODO: check

        SymbolTable.actualClass = superClass;
        superClass.consolidate();
        SymbolTable.actualClass = this;

        inheritAttributes(superClass);
        inheritMethods(superClass);
        is_consolidated = true;
    }

    private void inheritAttributes(Class superClass) {
        superClass.getAttributes().forEach(this::addPublicAttributes);
        superClass.getInstanceAttributes().reversed().forEach(instance_attributes::addFirst);
        superClass.getClassAttributes().reversed().forEach(class_attributes::addFirst);
    }

    private void inheritMethods(Class superClass) {
        superClass.getStaticMethods().reversed().forEach(this::inheritMethod);
        superClass.getDynamicMethods().reversed().forEach(this::inheritMethod);
    }

    private void inheritMethod(Method method) {
        List<Method> methods_list = method.isStatic()? static_methods_list:dynamic_methods_list;

        if (!methods.containsKey(method.getName())) {
            if (!method.isPrivate()) methods.put(method.getName(), method);
            methods_list.addFirst(method);
        } else {
            Method redefined = methods.get(method.getName());

            if (!method.isCompatible(redefined))
                SymbolTable.saveError(SemanticErrorMessages.METHOD_BAD_REDEFINED, redefined.getToken());

            if (!method.isPrivate()) {
                methods_list.remove(redefined);
                methods_list.addFirst(redefined);
            } else {
                methods_list.addFirst(method);
            }
        }
    }

    public void setSuperType(Type super_type) {
        if (hasTypeParameter(super_type.getName()))
            SymbolTable.saveError(SemanticErrorMessages.SUPERCLASS_GENERIC_TYPE, super_type.getToken());
        else this.super_type = super_type;
    }

    public boolean isAbstract() {
        return is_abstract;
    }

    public void setAbstract() {
        this.is_abstract = true;
    }

// ---------------------------------------- Methods -------------------------------------------------------------------

    public Method getMethod(String method_name) {
        return methods.get(method_name);
    }

    public List<Method> getStaticMethods() {
        return static_methods_list;
    }

    public List<Method> getDynamicMethods() {
        return dynamic_methods_list;
    }

    public void addMethod(String method_name, Method method) {
        if (methodNameAlreadyDefined(method_name)) {
            SymbolTable.saveError(SemanticErrorMessages.METHOD_ALREADY_DEFINED, method.getToken());
        } else {
            methods.put(method_name, method);
            if (method.isStatic()) static_methods_list.add(method);
            else dynamic_methods_list.addFirst(method);
        }
    }

// ------------------------------------- Constructors  ----------------------------------------------------------------

    public Constructor getConstructor(String constructor_name) {
        return constructors.get(constructor_name);
    }

    public void addConstructor(String constructor_name, Constructor constructor) {
        if (constructors.containsKey(constructor_name))
            SymbolTable.saveError(SemanticErrorMessages.CONSTRUCTOR_ALREADY_DEFINED, constructor.getToken());
        else constructors.put(constructor_name, constructor);
    }

// -------------------------------------- Abstract Methods  -----------------------------------------------------------

    public AbstractMethod getAbstractMethod(String method_name) {
        return abstractMethods.get(method_name);
    }

    public void addAbstractMethod(String method_name, AbstractMethod method) {
        if (methodNameAlreadyDefined(method_name))
            SymbolTable.saveError(SemanticErrorMessages.ABSTRACT_METHOD_ALREADY_DEFINED, method.getToken());
        else abstractMethods.put(method_name, method);
    }

    // ------------------------------------- Attributes  --------------------------------------------------------------

    public Attribute getAttribute(String attr_name) {
        return attributes.containsKey(attr_name)? attributes.get(attr_name).getFirst():null;
    }

    public Map<String, List<Attribute>> getAttributes() {
        return attributes;
    }

    public void addAttribute(String attr_name, Attribute attribute) {
        if (attributes.containsKey(attr_name))
            SymbolTable.saveError(SemanticErrorMessages.ATTRIBUTE_ALREADY_DEFINED, attribute.getToken());
        else {
            attributes.put(attr_name, new ArrayList<>(List.of(attribute)));
            if (attribute.isStatic()) class_attributes.addLast(attribute);
            else instance_attributes.addLast(attribute);
        }
    }

    public void addAttributes(String attr_name, List<Attribute> attr_list) {
        if (attr_list == null || attr_list.isEmpty()) return;
        if (attributes.containsKey(attr_name)) attributes.get(attr_name).addAll(attr_list);
        else attributes.put(attr_name, attr_list);
    }

    public List<Attribute> getInstanceAttributes() {
        return instance_attributes;
    }

    public List<Attribute> getClassAttributes() {
        return class_attributes;
    }

    private void addPublicAttributes(String attr_name, List<Attribute> attr_list) {
        addAttributes(attr_name, attr_list.stream().filter(attr -> !attr.isPrivate()).toList());
    }
// ------------------------------------- Generics --------------------------------------------------------------------

    public boolean hasTypeParameter(String type_param_name) {
        return type_parameters.containsKey(type_param_name);
    }

    public Token getTypeParameter(String type_param_name) {
        return type_parameters.get(type_param_name);
    }

    public void addTypeParameter(Token type_param_token) {
        if (type_parameters.containsKey(type_param_token.getLexeme()))
            SymbolTable.saveError(SemanticErrorMessages.GENERIC_TYPE_ALREADY_DEFINED, type_param_token);
        else type_parameters.put(type_param_token.getLexeme(), type_param_token);
    }

// -------------------------------------- Errors ---------------------------------------------------------------------
    private boolean methodNameAlreadyDefined(String unitName) {
        return (methods.containsKey(unitName) || abstractMethods.containsKey(unitName));
    }

    private void cyclicInheritance(Set<String> visited) throws SemanticException {
        if (Objects.equals(super_type.getName(), Object.name)) return;

        if (visited.contains(super_type.getName())) {
            SymbolTable.throwException(SemanticErrorMessages.CYCLIC_INHERITANCE, super_type.getToken());
        } else {
            visited.add(super_type.getName());
            if (SymbolTable.hasClass(super_type.getName()))
                SymbolTable.getClass(super_type.getName()).cyclicInheritance(visited);
        }
    }

    private void superNotDeclared() throws SemanticException {
        if (!SymbolTable.hasClass(super_type.getName()))
            SymbolTable.throwException(SemanticErrorMessages.CLASS_NOT_DECLARED, super_type.getToken());
    }
}
