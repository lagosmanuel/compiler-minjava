package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.statement.expression.Expression;

import java.util.ArrayList;
import java.util.List;

public class LocalVar extends Statement {
    private final Type type;
    private final Token identifier;
    private final Expression value;
    private final List<LocalVar> localVars = new ArrayList<>();

    public LocalVar(Type type, List<Token> identifiers, Expression value) {
        super(type.getToken()); // TODO
        this.type = type;
        this.identifier = null;
        this.value = value;
        identifiers.forEach(identifier -> localVars.add(new LocalVar(type, identifier, value)));
    }

    public LocalVar(Type type, List<Token> identifiers) {
        super(identifiers.getFirst());
        this.type = type;
        this.identifier = null;
        this.value = null;
        identifiers.forEach(identifier -> localVars.add(new LocalVar(type, identifier, null)));
    }

    public LocalVar(Type type, Token identifier, Expression value) {
        super(type.getToken());
        this.type = type;
        this.identifier = identifier;
        this.value = value;
        localVars.add(this);
    }

    public boolean hasLocalVar(String name) {
        return localVars.stream().anyMatch(localVar -> localVar.identifier.getLexeme().equals(name));
    }

    public LocalVar getLocalVar(String name) {
        return localVars.stream().filter(localVar -> localVar.identifier.getLexeme().equals(name)).findFirst().orElse(null);
    }

    public Type getType() {
        return type;
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        getParent().addLocalVar(this);
        type.validate();
        Type valueType = value != null? value.checkType():null;

        if (value != null && (valueType == null || !type.compatible(valueType)))
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.TYPE_NOT_COMPATIBLE,
                    type.getName(),
                    valueType != null? valueType.getName():"null"
                ),
                getIdentifier()
            );
    }
}
