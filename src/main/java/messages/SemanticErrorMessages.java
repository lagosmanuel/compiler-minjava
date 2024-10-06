package main.java.messages;

public class SemanticErrorMessages {
    public static final String CLASS_DUPLICATE = "Class %s is already defined";
    public static final String CLASS_NOT_DECLARED = "Class %s is not declared";
    public static final String PARAMETER_DUPLICATE = "Parameter %s is already declared";
    public static final String METHOD_DUPLICATE = "Method %s is already defined";
    public static final String CONSTRUCTOR_DUPLICATE = "Constructor is already defined";
    public static final String ABSTRACT_METHOD_DUPLICATE = "Abstract method %s is already defined";
    public static final String ATTRIBUTE_DUPLICATE = "Attribute %s is already defined";
    public static final String GENERIC_TYPE_DUPLICATE = "Type Parameter %s is already defined";
    public static final String SUPERCLASS_GENERIC_TYPE = "A class cannot extend a generic type";
    public static final String CYCLIC_INHERITANCE = "Cyclic inheritance detected";
    public static final String ATTRIBUTE_VOID = "Attribute cannot be of type void";
    public static final String TYPE_NOTFOUND = "Type %s not found";
    public static final String ABSTRACT_METHOD_IN_NON_ABSTRACT_CLASS = "Abstract method in non-abstract class";
    public static final String CONSTRUCTOR_NAME_MISMATCH = "Constructor name does not match the class name";
    public static final String ABSTRACT_METHOD_PRIVATE = "Abstract method cannot be private";
    public static final String ABSTRACT_METHOD_STATIC = "Abstract method cannot be static";
    public static final String MAIN_NOT_FOUND = "Main method not found";
    public static final String METHOD_BAD_REDEFINED = "Method redefinition is not compatible";
    public static final String INVALID_TYPE_PARAMETERS_COUNT = "Wrong number of type arguments; required %d";
    public static final String TYPE_PARAMETER_RECURSIVE = "Type parameter cannot have type parameters";
    public static final String ABSTRACT_METHOD_REDEFINED = "Abstract method %s is already declared in the super class";
    public static final String ABSTRACT_METHOD_BAD_IMPLEMENTED = "Abstract method implementation is not compatible";
    public static final String ABSTRACT_METHOD_NOT_IMPLEMENTED = "Class %s is not abstract and does not implement abstract method %s";
    public static final String ABSTRACT_METHOD_BAD_OVERRIDE = "Abstract method %s is not compatible with the super class implementation";
}
