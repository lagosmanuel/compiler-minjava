package main.java.semantic.entities.model.statement;

import main.java.exeptions.SemanticException;
import main.java.messages.SemanticErrorMessages;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.semantic.SymbolTable;
import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.model.type.TypeVar;
import main.java.semantic.entities.model.statement.expression.Expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocalVar extends Statement {
    private Type type;
    private final Token identifier;
    private final Expression value;
    private final List<LocalVar> localVars = new ArrayList<>();

    public LocalVar(Type type, List<Token> identifiers, Expression value) {
        super(type.getToken());
        this.type = type;
        this.identifier = type.getToken();
        this.value = value;
        identifiers.forEach(identifier -> localVars.add(new LocalVar(type, identifier, value)));
    }

    public LocalVar(Type type, List<Token> identifiers) {
        super(type.getToken());
        this.type = type;
        this.identifier = type.getToken();
        this.value = null;
        identifiers.forEach(identifier -> localVars.add(new LocalVar(type, identifier, null)));
    }

    private LocalVar(Type type, Token identifier, Expression value) {
        super(identifier);
        this.type = type;
        this.identifier = identifier;
        this.value = value;
    }

    public List<LocalVar> getLocalVars() {
        return localVars;
    }

    public Type getType() {
        return type;
    }

    private void setType(Type type) {
        this.type = type;
        localVars.forEach(localVar -> localVar.setType(type));
    }

    @Override
    public void check() throws SemanticException {
        if (checked()) return;
        super.check();
        if (type == null) return;

        type.validate();
        Type valueType = value != null? value.checkType():null;
        for (TypeVar typeVar:type.getTypeParams()) typeVar.check();

        if (valueType != null && !type.compatible(valueType)) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.TYPE_NOT_COMPATIBLE,
                    type.getName(),
                    valueType.getName()
                ),
                getIdentifier() // TODO: Check if this is the correct token
            );
        }

        for (LocalVar localVar:localVars) {
            if (alreadyDeclared(localVar.getIdentifier())) continue;
            localVar.setParent(getParent());
            if (Objects.equals(type.getToken().getType(), TokenType.kwVar)) localVar.setType(valueType);
            if (getParent() != null) getParent().addLocalVar(localVar);
        }
    }

    private boolean alreadyDeclared(Token identifier) throws SemanticException {
        boolean declared = true;
        if (SymbolTable.actualUnit.getParameter(identifier.getLexeme()) != null) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.VARIABLE_ALREADY_DECLARED_PARAMETER,
                    identifier.getLexeme()
                ),
                identifier
            );
        } else if (getParent() != null && getParent().hasLocalVar(identifier.getLexeme())) {
            SymbolTable.throwException(
                String.format(
                    SemanticErrorMessages.VARIABLE_ALREADY_DECLARED,
                    identifier.getLexeme()
                ),
                identifier
            );
        } else declared = false;
        return declared;
    }
}
