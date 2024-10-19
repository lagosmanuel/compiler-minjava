package main.java.messages;

public class SemanticErrorMessages {
    public static final String CLASS_DUPLICATE = "Class %s is already defined";
    public static final String CLASS_NOT_DECLARED = "Class %s is not declared";
    public static final String PARAMETER_DUPLICATE = "Parameter %s is already declared";
    public static final String METHOD_DUPLICATE = "Method %s is already defined";
    public static final String CONSTRUCTOR_DUPLICATE = "Constructor is already defined";
    public static final String ABSTRACT_METHOD_DUPLICATE = "Abstract method %s is already defined";
    public static final String ATTRIBUTE_DUPLICATE = "Attribute %s is already declared";
    public static final String GENERIC_TYPE_DUPLICATE = "Type Parameter %s is already declared";
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
    public static final String ABSTRACT_METHOD_BAD_REDEFINED = "Abstract method %s is not compatible with the super class declaration";
    public static final String ABSTRACT_METHOD_BAD_IMPLEMENTED = "Abstract method implementation is not compatible with the super class declaration";
    public static final String ABSTRACT_METHOD_NOT_IMPLEMENTED = "Class %s is not abstract and does not implement abstract method %s";
    public static final String ABSTRACT_METHOD_BAD_OVERRIDE = "Abstract method %s is not compatible with the super class implementation";

    public static final String IF_CONDITION_NOT_BOOLEAN = "If condition must be of type boolean; found %s";
    public static final String WHILE_CONDITION_NOT_BOOLEAN = "While condition must be of type boolean; found %s";
    public static final String RETURN_TYPE_MISMATCH = "Return type mismatch; expected %s, found %s";
    public static final String RETURN_NULL = "Return statement with no value in a method that returns %s";
    public static final String TYPE_NOT_COMPATIBLE = "Type not compatible; expected %s or subtype, found %s";
    public static final String ASSIGN_INCREMENT_TYPE_NOT_NUMERIC = "Assignment or increment operation requires a numeric type; found %s";
    public static final String FOR_CONDITION_NOT_BOOLEAN = "For condition must be of type boolean; found %s";
    public static final String FOR_INCREMENT_NOT_ASSIGNMENT = "For increment must be an assignment";
    public static final String CASE_EXPRESSION_TYPE_NOT_COMPATIBLE = "Case expression type not compatible; expected %s, found %s";
    public static final String BREAK_OUTSIDE_LOOP = "Break statement outside loop";
    public static final String LITERAL_INVALID_TYPE = "Invalid literal type";
    public static final String PLUS_MINUS_OPERAND_NOT_NUMERIC = "Operand of unary operator %s must be numeric; found %s";
    public static final String NOT_CONDITION_NOT_BOOLEAN = "Operand of unary operator %s must be boolean; found %s";
    public static final String UNARYOP_INVALID = "Invalid unary operator %s";
    public static final String BINARY_OPERAND_NOT_NUMERIC = "Binary operator %s requires numeric operands; found %s and %s";
    public static final String BINARY_OPERAND_NOT_BOOLEAN = "Binary operator %s requires boolean operands; found %s and %s";
    public static final String BINARY_OPERAND_NOT_COMPATIBLE = "Binary operator %s requires compatible operands; found %s and %s";
    public static final String BINARYOP_INVALID = "Invalid binary operator %s";
    public static final String ATTR_NON_STATIC_ACCESS = "Non-static variable %s cannot be accessed from a static context";
    public static final String IDENTIFIER_NOT_FOUND = "Identifier %s not found";
    public static final String CONSTRUCTOR_NOT_DECLARED = "Constructor with %d arguments not declared";
    public static final String CONSTRUCTOR_WRONG_NUMBER_OF_TYPE_VARS = "Wrong number of type arguments; required %d, found %d";
    public static final String METHOD_NOT_FOUND = "Method %s with %d parameters not found in class %s";
    public static final String METHOD_WRONG_NUMBER_OF_ARGUMENTS = "Method %s in class %s requires %d arguments; found %d";
    public static final String METHOD_NON_STATIC = "Non-static method %s cannot be referenced from a static context";
    public static final String METHOD_PRIVATE = "Method %s has private access in class %s";
    public static final String ATTRIBUTE_NOT_FOUND = "Attribute %s not found in class %s";
    public static final String ATTRIBUTE_PRIVATE = "Attribute %s has private access in class %s";
}
