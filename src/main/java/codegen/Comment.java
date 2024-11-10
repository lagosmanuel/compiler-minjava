package main.java.codegen;

public class Comment {
    public static final String CLASS_VT = "; (Virtual Table of class %s)";
    public static final String CLASS_CODE = "; (Code of class %s)";
    public static final String SAVE_FP = "; Save FP register";
    public static final String RESTORE_FP = "; Restore FP register";
    public static final String LOAD_SP = "; Load SP register";
    public static final String STORE_FP = "; Store FP register";
    public static final String MAIN_CALL = "; call the main function";
    public static final String HEAP_INIT_CALL = "; call the heap init function";
    public static final String HEAP_INIT = "; heap init function";
    public static final String MALLOC = "; malloc function";
    public static final String BLOCK_RET = "; return from block and free local variables";
    public static final String UNIT_RET = "; return from unit and free params";
    public static final String LOAD_THIS = "; Load reference to this";
    public static final String SUPER_CALL = "; Call super constructor";
    public static final String SUPER_LOAD = "; Load super constructor";
    public static final String LITERAL_LOAD = "; Load literal: %s";
    public static final String OP_NEG = "; Negate operand";
    public static final String OP_NOT = "; Boolean Not operand";
    public static final String OP_BINARY = "; Binary operator %s";
    public static final String EXPRESSION_DROP_VALUE = "; Drop value from expression";
    public static final String VAR_LOAD = "; Load variable/parameter: %s";
    public static final String VAR_STORE = "; Store variable/parameter: %s";
    public static final String ATTRIBUTE_LOAD = "Load attribute: %s";
    public static final String ATTRIBUTE_STATIC_LOAD = "; Load static attribute: %s";
    public static final String ATTRIBUTE_STORE = "; Store attribute: %s";
    public static final String ATTRIBUTE_STATIC_STORE = "; Store static attribute: %s";
    public static final String ASSIGN_PLUS = "; Calculate assignment plus";
    public static final String ASSIGN_MINUS = "; Calculate assignment minus";
    public static final String RETURN_ALLOC = "; Reserve space for return value of method %s";
    public static final String CALL_METHOD = "; Call method %s";
    public static final String VT_ACCESS_METHOD = "; Get method %s from VT";
    public static final String ACCESS_STATIC_METHOD = "; Get static method %s";
    public static final String SWAP_ARGUMENTS = "; Swap method arguments and reference";
    public static final String IPRINT = "; print integer value from stack";
}
