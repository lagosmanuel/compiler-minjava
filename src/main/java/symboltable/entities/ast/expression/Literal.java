package main.java.symboltable.entities.ast.expression;

import main.java.model.Token;
import main.java.symboltable.SymbolTable;
import main.java.symboltable.entities.type.Type;
import main.java.symboltable.entities.type.ClassType;
import main.java.symboltable.entities.type.PrimitiveType;
import main.java.symboltable.entities.predefined.Character;
import main.java.codegen.Instruction;
import main.java.codegen.Comment;
import main.java.codegen.Strings;
import main.java.config.CodegenConfig;
import main.java.messages.SemanticErrorMessages;
import main.java.messages.CodegenErrorMessages;
import main.java.exeptions.SemanticException;

public class Literal extends Operand {
    private final Token value;
    private String label;

    public Literal(Token value) {
        super(value);
        this.value = value;
    }

    @Override
    public Type checkType() throws SemanticException {
        if (value == null) return null;
        return switch (value.getType()) {
            case intLiteral -> PrimitiveType.INT_TYPE;
            case floatLiteral -> PrimitiveType.FLOAT_TYPE;
            case charLiteral -> PrimitiveType.CHAR_TYPE;
            case trueLiteral, falseLiteral -> PrimitiveType.BOOLEAN_TYPE;
            case stringLiteral -> ClassType.STRING_TYPE;
            case nullLiteral -> ClassType.NULL_TYPE;
            default -> {
                SymbolTable.throwException(
                    SemanticErrorMessages.LITERAL_INVALID_TYPE,
                    value
                );
                yield null;
            }
        };
    }

    @Override
    public void generate() {
        if (value == null) return;
        switch (value.getType()) {
            case intLiteral -> SymbolTable.getGenerator().write(
                Instruction.PUSH.toString(),
                value.getLexeme(),
                Comment.LITERAL_LOAD.formatted(value.getLexeme())
            );
            case floatLiteral -> throw new RuntimeException(
                CodegenErrorMessages.FLOAT_NOT_SUPPORTED
            );
            case charLiteral -> SymbolTable.getGenerator().write(
                Instruction.PUSH.toString(),
                String.valueOf((int) Character.extract(value.getLexeme())),
                Comment.LITERAL_LOAD.formatted(value.getLexeme())
            );
            case trueLiteral -> SymbolTable.getGenerator().write(
                Instruction.PUSH.toString(),
                CodegenConfig.TRUE_VALUE,
                Comment.LITERAL_LOAD.formatted(value.getLexeme())
            );
            case falseLiteral -> SymbolTable.getGenerator().write(
                Instruction.PUSH.toString(),
                CodegenConfig.FALSE_VALUE,
                Comment.LITERAL_LOAD.formatted(value.getLexeme())
            );
            case stringLiteral -> {
                Strings.create(value.getLexeme());
            }
            case nullLiteral -> SymbolTable.getGenerator().write(
                Instruction.PUSH.toString(),
                CodegenConfig.NULL_VALUE,
                Comment.LITERAL_LOAD.formatted(value.getLexeme())
            );
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
