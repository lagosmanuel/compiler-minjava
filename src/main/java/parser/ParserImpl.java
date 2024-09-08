package main.java.parser;

import main.java.lexer.Lexer;
import main.java.model.Error;
import main.java.model.Pair;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.config.ParserConfig;
import main.java.exeptions.LexicalException;
import main.java.exeptions.SyntacticException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ParserImpl implements Parser {
    private final Map<Integer, Pair<List<Error>, String>> errors;
    private final Lexer lexer;
    private Token token;

    public ParserImpl(Lexer lexer) {
        this.lexer = lexer;
        this.errors = new HashMap<>();
    }

    @Override
    public void parse() throws SyntacticException {
        token = getToken();
        Start();
        match(TokenType.EOF);
    }

    @Override
    public Map<Integer, Pair<List<Error>, String>> getErrors() {
        return errors;
    }

    private void match(TokenType tokenType) throws SyntacticException {
        if (token == null || token.getType() != tokenType)
            throwException();
        token = getToken();
    }

    private void Start() throws SyntacticException {
        ClassList();
    }

//------------------------------------------------------------------------------

    private void ClassList() throws SyntacticException {
        switch (token.getType()) {
            case kwClass -> {
                Class();
                ClassList();
            }
            case EOF -> {
                return;
            }
            default -> throwException();
        }
    }

    private void Class() throws SyntacticException {
        match(TokenType.kwClass);
        match(TokenType.idClassVar);
        InheritanceOptional();
        match(TokenType.leftBrace);
        MemberList();
        match(TokenType.rightBrace);
    }

    private void InheritanceOptional() throws SyntacticException {
        switch (token.getType()) {
            case kwExtends -> {
                match(TokenType.kwExtends);
                match(TokenType.idClassVar);
            }
            case leftBrace -> {
                return;
            }
            default -> throwException();
        }
    }

    private void MemberList() throws SyntacticException {
        if (token.getType() == TokenType.kwStatic || Lookup.Member.contains(token.getType())) {
            Member();
            MemberList();
        } else if (token.getType() == TokenType.rightBrace) {
            return;
        } else {
            throwException();
        }
    }

    private void Member() throws SyntacticException {
        if (token.getType() == TokenType.kwStatic || Lookup.MemberType.contains(token.getType())) {
            StaticOptional();
            MemberType();
            match(TokenType.idMetVar);
            MemberRest();
        } else if (Lookup.Constructor.contains(token.getType())) {
            Constructor();
        } else {
            throwException();
        }
    }

    private void MemberRest() throws SyntacticException {
        switch (token.getType()) {
            case semicolon -> match(TokenType.semicolon);
            case leftParenthesis -> {
                FormalArgs();
                Block();
            }
            default -> throwException();
        }
    }

    private void Constructor() throws SyntacticException {
        match(TokenType.kwPublic);
        match(TokenType.idClassVar);
        FormalArgs();
        Block();
    }

    private void StaticOptional() throws SyntacticException {
        if (token.getType() == TokenType.kwStatic) {
            match(TokenType.kwStatic);
        } else if (Lookup.MemberType.contains(token.getType())) {
            return;
        } else {
            throwException();
        }
    }

    private void MemberType() throws SyntacticException {
        if (token.getType() == TokenType.kwVoid) {
            match(TokenType.kwVoid);
        } else if (Lookup.Type.contains(token.getType())) {
            Type();
        } else {
            throwException();
        }
    }

    private void Type() throws SyntacticException {
        switch (token.getType()) {
            case kwBoolean, kwChar, kwInt -> PrimitiveType();
            case idClassVar -> match(TokenType.idClassVar);
            default -> throwException();
        }
    }

    private void PrimitiveType() throws SyntacticException {
        switch (token.getType()) {
            case kwBoolean -> match(TokenType.kwBoolean);
            case kwChar -> match(TokenType.kwChar);
            case kwInt -> match(TokenType.kwInt);
            default -> throwException();
        }
    }

    private void FormalArgs() throws SyntacticException {
        match(TokenType.leftParenthesis);
        FormalArgsListOptional();
        match(TokenType.rightParenthesis);
    }

    private void FormalArgsListOptional() throws SyntacticException {
        if (token.getType() == TokenType.rightParenthesis) {
            return;
        } else if (Lookup.Type.contains(token.getType())) {
            FormalArgsList();
        } else {
            throwException();
        }
    }

    private void FormalArgsList() throws SyntacticException {
        FormalArg();
        FormalArgsListRest();
    }

    private void FormalArgsListRest() throws SyntacticException {
        switch (token.getType()) {
            case comma -> {
                match(TokenType.comma);
                FormalArgsList();
            }
            case rightParenthesis -> {
                return;
            }
            default -> throwException();
        }
    }

    private void FormalArg() throws SyntacticException {
        Type();
        match(TokenType.idMetVar);
    }

//------------------------------------------------------------------------------

    private void Block() throws SyntacticException {
        match(TokenType.leftBrace);
        StatementList();
        match(TokenType.rightBrace);
    }

    private void StatementList() throws SyntacticException {
        if (Lookup.Statement.contains(token.getType())) {
            Statement();
            StatementList();
        } else if (token.getType() == TokenType.rightBrace) {
            return;
        } else {
            throwException();
        }
    }

    private void StatementOptional() throws SyntacticException {
        if (Lookup.Statement.contains(token.getType())) {
            Statement();
        } else if (token.getType() == TokenType.rightBrace ||
                token.getType() == TokenType.kwCase ||
                token.getType() == TokenType.kwDefault) {
            return;
        } else {
            throwException();
        }
    }

    private void Statement() throws SyntacticException {
        switch (token.getType()) {
            case semicolon -> match(TokenType.semicolon);
            case kwVar -> {
                LocalVar();
                match(TokenType.semicolon);
            }
            case kwReturn -> {
                Return();
                match(TokenType.semicolon);
            }
            case kwBreak -> {
                Break();
                match(TokenType.semicolon);
            }
            case kwIf -> If();
            case kwWhile -> While();
            case kwSwitch -> Switch();
            case leftBrace -> Block();
            default -> {
               if (Lookup.Expression.contains(token.getType())) {
                   Expression();
                   match(TokenType.semicolon);
               } else {
                   throwException();
               }
            }
        }
    }

    private void LocalVar() throws SyntacticException {
        match(TokenType.kwVar);
        match(TokenType.idMetVar);
        match(TokenType.opEqual);
        CompositeExpression();
    }

    private void Return() throws SyntacticException {
        match(TokenType.kwReturn);
        ExpressionOptional();
    }

    private void Break() throws SyntacticException {
        match(TokenType.kwBreak);
    }

    private void If() throws SyntacticException {
        match(TokenType.kwIf);
        match(TokenType.leftParenthesis);
        Expression();
        match(TokenType.rightParenthesis);
        Statement();
        IfRest();
    }

    private void IfRest() throws SyntacticException {
        if (Objects.requireNonNull(token.getType()) == TokenType.kwElse) {
            match(TokenType.kwElse);
            Statement();
        } else {
            /* TODO: calculate follow set of IfRest
            throwException(); */
        }
    }

    private void While() throws SyntacticException {
        match(TokenType.kwWhile);
        match(TokenType.leftParenthesis);
        Expression();
        match(TokenType.rightParenthesis);
        Statement();
    }

    private void Switch() throws SyntacticException {
        match(TokenType.kwSwitch);
        match(TokenType.leftParenthesis);
        Expression();
        match(TokenType.rightParenthesis);
        match(TokenType.leftBrace);
        SwitchStatementList();
        match(TokenType.rightBrace);
    }

    private void SwitchStatementList() throws SyntacticException {
        switch (token.getType()) {
            case kwCase, kwDefault -> {
                SwitchStatement();
                SwitchStatementList();
            }
            case rightBrace -> {
                return;
            }
            default -> throwException();
        }
    }

    private void SwitchStatement() throws SyntacticException {
        switch (token.getType()) {
            case kwCase -> {
                match(TokenType.kwCase);
                PrimitiveLiteral();
                match(TokenType.colon);
                StatementOptional();
            }
            case kwDefault -> {
                match(TokenType.kwDefault);
                match(TokenType.colon);
                Statement();
            }
            default -> throwException();
        }
    }

//------------------------------------------------------------------------------

    private void ExpressionOptional() throws SyntacticException {
        if (Lookup.Expression.contains(token.getType())) {
            Expression();
        } else if (token.getType() == TokenType.semicolon) {
            return;
        } else {
            throwException();
        }
    }

    private void Expression() throws SyntacticException {
        CompositeExpression();
        ExpressionRest();
    }

    private void ExpressionRest() throws SyntacticException {
        switch (token.getType()) {
            case opAssign, opPlusAssign, opMinusAssign -> {
                AssignmentOp();
                CompositeExpression();
            }
            /* TODO: calculate follow set of ExpressionRest
            default -> throwException();
            */
        }
    }

    private void AssignmentOp() throws SyntacticException {
        switch (token.getType()) {
            case opAssign -> match(TokenType.opAssign);
            case opPlusAssign -> match(TokenType.opPlusAssign);
            case opMinusAssign -> match(TokenType.opMinusAssign);
            default -> throwException();
        }
    }

    private void CompositeExpression() throws SyntacticException {
        BasicExpression();
        CompositeExpressionRest();
    }

    private void CompositeExpressionRest() throws SyntacticException {
        if (Lookup.BinaryOp.contains(token.getType())) {
            BinaryOp();
            BasicExpression();
            CompositeExpressionRest();
        } else {
            /* TODO: calculate follow set of CompositeExpressionRest
            throwException(); */
        }
    }

    private void BinaryOp() throws SyntacticException {
        switch (token.getType()) {
            case opOr -> match(TokenType.opOr);
            case opAnd -> match(TokenType.opAnd);
            case opEqual -> match(TokenType.opEqual);
            case opNotEqual -> match(TokenType.opNotEqual);
            case opLess -> match(TokenType.opLess);
            case opGreater -> match(TokenType.opGreater);
            case opLessEqual -> match(TokenType.opLessEqual);
            case opGreaterEqual -> match(TokenType.opGreaterEqual);
            case opPlus -> match(TokenType.opPlus);
            case opMinus -> match(TokenType.opMinus);
            case opTimes -> match(TokenType.opTimes);
            case opDiv -> match(TokenType.opDiv);
            case opMod -> match(TokenType.opMod);
            default -> throwException();
        }
    }

    private void BasicExpression() throws SyntacticException {
        if (token.getType() == TokenType.opPlus || token.getType() == TokenType.opMinus || token.getType() == TokenType.opNot) {
            UnaryOp();
            Operand();
        } else if (Lookup.Operand.contains(token.getType())) {
            Operand();
        } else {
            throwException();
        }
    }

    private void UnaryOp() throws SyntacticException {
        switch (token.getType()) {
            case opPlus -> match(TokenType.opPlus);
            case opMinus -> match(TokenType.opMinus);
            case opNot -> match(TokenType.opNot);
            default -> throwException();
        }
    }

    private void Operand() throws SyntacticException {
        if (Lookup.Literal.contains(token.getType())) {
            Literal();
        } else if (Lookup.Access.contains(token.getType())) {
            Access();
        } else {
            throwException();
        }
    }

    private void Literal() throws SyntacticException {
        if (Lookup.PrimitiveLiteral.contains(token.getType())) {
            PrimitiveLiteral();
        } else if (Lookup.ObjectLiteral.contains(token.getType())) {
            ObjectLiteral();
        } else {
            throwException();
        }
    }

    private void PrimitiveLiteral() throws SyntacticException {
        switch (token.getType()) {
            case trueLiteral -> match(TokenType.trueLiteral);
            case falseLiteral -> match(TokenType.falseLiteral);
            case intLiteral -> match(TokenType.intLiteral);
            case charLiteral -> match(TokenType.charLiteral);
            default -> throwException();
        }
    }

    private void ObjectLiteral() throws SyntacticException {
        switch (token.getType()) {
            case nullLiteral -> match(TokenType.nullLiteral);
            case stringLiteral -> match(TokenType.stringLiteral);
            default -> throwException();
        }
    }

    private void Access() throws SyntacticException {
        Primary();
        ChainedOptional();
    }

    private void Primary() throws SyntacticException {
        switch (token.getType()) {
            case kwThis -> ThisAccess();
            case idMetVar -> IdMetVarAccess();
            case kwNew -> ConstructorAccess();
            case idClassVar -> StaticMethodAccess();
            case leftParenthesis -> ParentizedExpression();
            default -> throwException();
        }
    }

    private void ThisAccess() throws SyntacticException {
        match(TokenType.kwThis);
    }

    private void IdMetVarAccess() throws SyntacticException {
        match(TokenType.idMetVar);
        IdMetVarAccessRest();
    }

    private void IdMetVarAccessRest() throws SyntacticException {
        if (Objects.requireNonNull(token.getType()) == TokenType.leftParenthesis) {
            ActualArgs();
        } else {
            /* TODO: calculate follow set of IdMetVarAccessRest
            throwException(); */
        }
    }

    private void ConstructorAccess() throws SyntacticException {
        match(TokenType.kwNew);
        match(TokenType.idClassVar);
        ActualArgs();
    }

    private void StaticMethodAccess() throws SyntacticException {
        match(TokenType.idClassVar);
        match(TokenType.dot);
        match(TokenType.idMetVar);
        ActualArgs();
    }

    private void ParentizedExpression() throws SyntacticException {
        match(TokenType.leftParenthesis);
        Expression();
        match(TokenType.rightParenthesis);
    }

    private void ActualArgs() throws SyntacticException {
        match(TokenType.leftParenthesis);
        ExpressionListOptional();
        match(TokenType.rightParenthesis);
    }

    private void ExpressionListOptional() throws SyntacticException {
        if (Lookup.Expression.contains(token.getType())) {
            ExpressionList();
        } else if (token.getType() == TokenType.rightParenthesis) {
            return;
        } else {
            throwException();
        }
    }

    private void ExpressionList() throws SyntacticException {
        Expression();
        ExpressionListRest();
    }

    private void ExpressionListRest() throws SyntacticException {
        switch (token.getType()) {
            case comma -> {
                match(TokenType.comma);
                ExpressionList();
            }
            case rightParenthesis -> {
                return;
            }
            default -> throwException();
        }
    }

    private void ChainedOptional() throws SyntacticException {
        if (Objects.requireNonNull(token.getType()) == TokenType.dot) {
            ChainedIdMetVar();
        } else {
            /* TODO: calculate follow set of ChainedOptional
            throwException(); */
        }
    }

    private void ChainedIdMetVar() throws SyntacticException {
        match(TokenType.dot);
        match(TokenType.idMetVar);
        ChainedIdMetVarRest();
    }

    private void ChainedIdMetVarRest() throws SyntacticException {
        switch (token.getType()) {
            case dot -> ChainedOptional();
            case leftParenthesis -> {
                ActualArgs();
                ChainedOptional();
            }
            /* TODO: calculate follow set of ChainedIdMetVarRest
            default -> throwException();
            */
        }
    }

//------------------------------------------------------------------------------

    private Token getToken() throws SyntacticException {
        while (true) {
            try {
                return lexer.nextToken();
            } catch (LexicalException exception) {
                if (!ParserConfig.CONTINUE_ON_ERROR)
                    throw new SyntacticException(exception.getMessage());
            }
        }
    }

    private void throwException() throws SyntacticException {
        throw new SyntacticException(String.format("[Error:%s|%d]", token.getLexeme(), token.getLine()));
    }
}