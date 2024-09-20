package main.java.parser;

import main.java.lexer.Lexer;
import main.java.model.Error;
import main.java.model.Pair;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.model.ErrorType;
import main.java.config.ParserConfig;
import main.java.exeptions.LexicalException;
import main.java.exeptions.SyntacticException;
import main.java.utils.Formater;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParserImpl implements Parser {
    private final Map<Integer, Pair<List<Error>, String>> errors;
    private final Lexer lexer;
    private Token token;
    private boolean panic_mode;

    public ParserImpl(Lexer lexer, Map<Integer, Pair<List<Error>, String>> errors) {
        this.lexer = lexer;
        this.errors = errors;
    }

    @Override
    public void parse() throws SyntacticException {
        token = getToken();
        Start();
        if (token != null && token.getType() != TokenType.EOF)
            consumeTokens();
    }

    private void match(TokenType tokenType) throws SyntacticException {
        if (token == null || token.getType() != tokenType)
            throwException(List.of(tokenType.toString()));
        else {
            token = getToken();
            if (panic_mode) panic_mode = false;
        }
    }

    private void Start() throws SyntacticException {
        ClassList();
        match(TokenType.EOF);
    }

//------------------------------------------------------------------------------

    private void ClassList() throws SyntacticException {
        if (Lookup.Class.contains(token.getType())) {
            Class();
            ClassList();
        } else if (token.getType() == TokenType.EOF) {
            return;
        } else throwException(List.of(
            TokenType.kwAbstract.toString(),
            TokenType.kwClass.toString(),
            TokenType.EOF.toString()
        ));

        while (panic_mode && token.getType() != TokenType.EOF) {
            if (token.getType() == TokenType.leftBrace) Class();
            if (Lookup.Class.contains(token.getType())) ClassList();

            if (panic_mode && token.getType() != TokenType.EOF) {
                token = getToken();
                recoverFromError();
            }
        }
    }

    private void Class() throws SyntacticException {
        AbstractOptional();
        match(TokenType.kwClass);
        ClassType();
        InheritanceOptional();
        match(TokenType.leftBrace);
        if (!panic_mode) MemberList();
        match(TokenType.rightBrace);
    }

    private void InheritanceOptional() throws SyntacticException {
        switch (token.getType()) {
            case kwExtends -> {
                match(TokenType.kwExtends);
                ClassType();
            }
            case leftBrace -> {
                return;
            }
            default -> throwException(List.of(
                TokenType.kwExtends.toString(),
                TokenType.leftBrace.toString()
            ));
        }
    }

    private void MemberList() throws SyntacticException {
        if (Lookup.Member.contains(token.getType())) {
            VisibilityOptional();
            Member();
            MemberList();
        } else if (token.getType() == TokenType.rightBrace) {
            return;
        } else {
            throwException(List.of(
                "a member",
                TokenType.rightBrace.toString()
            ));
        }

        while (panic_mode && token.getType() != TokenType.EOF) {
            if (Lookup.Statement.contains(token.getType())) {StatementList(); match(TokenType.rightBrace);}
            if (Lookup.Member.contains(token.getType())) MemberList();
            if (Lookup.Class.contains(token.getType())) ClassList();

            if (panic_mode && token.getType() != TokenType.EOF) {
                token = getToken();
                recoverFromError();
            }
        }
    }

    private void Member() throws SyntacticException {
        switch (token.getType()) {
            case idClass -> {
                match(TokenType.idClass);
                MaybeConstructor();
            }
            case kwAbstract -> {
                match(TokenType.kwAbstract);
                MemberType();
                match(TokenType.idMetVar);
                FormalArgs();
                match(TokenType.semicolon);
            }
            case kwStatic -> {
                match(TokenType.kwStatic);
                MemberType();
                match(TokenType.idMetVar);
                MemberRest();
            }
            case kwVoid -> {
                match(TokenType.kwVoid);
                match(TokenType.idMetVar);
                MemberRest();
            }
            case kwBoolean, kwChar, kwInt, kwFloat -> {
                PrimitiveType();
                match(TokenType.idMetVar);
                MemberRest();
            }
            default -> throwException(List.of(
            "a member"
            ));
        }
    }

    private void MaybeConstructor() throws SyntacticException {
        switch (token.getType()) {
            case leftParenthesis -> {
                FormalArgs();
                Block();
            }
            case opLess, idMetVar -> {
                GenericTypeOptional();
                match(TokenType.idMetVar);
                MemberRest();
            }
            default -> throwException(List.of(
                TokenType.idMetVar.toString(),
                "a parameterized type",
                "formal arguments and a block"
            ));
        }
    }

    private void MemberRest() throws SyntacticException {
        switch (token.getType()) {
            case opAssign, semicolon -> {
                AssignmentOptional();
                match(TokenType.semicolon);
            }
            case leftParenthesis -> {
                FormalArgs();
                Block();
            }
            default -> throwException(List.of(
                TokenType.opAssign.toString(),
                TokenType.semicolon.toString(),
                "formal arguments and a block"
            ));
        }
    }

    private void AbstractOptional() throws SyntacticException {
        switch (token.getType()) {
            case kwAbstract -> match(TokenType.kwAbstract);
            case kwClass -> {
                return;
            }
            default -> throwException(List.of(
                TokenType.kwAbstract.toString(),
                TokenType.kwClass.toString()
            ));
        }
    }

    private void VisibilityOptional() throws SyntacticException {
        switch (token.getType()) {
            case kwPublic -> match(TokenType.kwPublic);
            case kwPrivate -> match(TokenType.kwPrivate);
            default -> {
               if (Lookup.Member.contains(token.getType())) return;
               else throwException(List.of(
                   TokenType.kwPublic.toString(),
                   TokenType.kwPrivate.toString(),
                   "a member"
               ));
            }
        }
    }

    private void MemberType() throws SyntacticException {
        if (token.getType() == TokenType.kwVoid) {
            match(TokenType.kwVoid);
        } else if (Lookup.Type.contains(token.getType())) {
            Type();
        } else {
            throwException(List.of(
            "a type"
            ));
        }
    }

    private void Type() throws SyntacticException {
        switch (token.getType()) {
            case kwBoolean, kwChar, kwInt, kwFloat -> PrimitiveType();
            case idClass -> ClassType();
            default -> throwException(List.of(
                "a primitive type",
                "a class type"
            ));
        }
    }

    private void PrimitiveType() throws SyntacticException {
        switch (token.getType()) {
            case kwBoolean -> match(TokenType.kwBoolean);
            case kwChar -> match(TokenType.kwChar);
            case kwInt -> match(TokenType.kwInt);
            case kwFloat -> match(TokenType.kwFloat);
            default -> throwException(List.of(
                TokenType.kwBoolean.toString(),
                TokenType.kwChar.toString(),
                TokenType.kwInt.toString(),
                TokenType.kwFloat.toString()
            ));
        }
    }

    private void ClassType() throws SyntacticException {
        match(TokenType.idClass);
        GenericTypeOptional();
    }

    private void GenericTypeOptional() throws SyntacticException {
        switch (token.getType()) {
            case opLess -> {
                match(TokenType.opLess);
                GenericTypeList();
                match(TokenType.opGreater);
            }
            case idMetVar, kwExtends, leftBrace -> {
                return;
            }
            default -> throwException(List.of(
                "a parameterized type",
                TokenType.idMetVar.toString(),
                TokenType.kwExtends.toString(),
                TokenType.leftBrace.toString()
            ));
        }
    }

    private void GenericTypeList() throws SyntacticException {
        match(TokenType.idClass);
        GenericTypeListRest();
    }

    private void GenericTypeListRest() throws SyntacticException {
        switch (token.getType()) {
            case comma -> {
                match(TokenType.comma);
                GenericTypeList();
            }
            case opGreater -> {
                return;
            }
            default -> throwException(List.of(
                TokenType.comma.toString(),
                TokenType.opGreater.toString()
            ));
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
            throwException(List.of(
                "a formal parameter list",
                TokenType.rightParenthesis.toString()
            ));
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
            default -> throwException(List.of(
                TokenType.rightParenthesis.toString(),
                "a comma and another formal parameter"
            ));
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
        } else throwException(List.of(
            "a statement",
            TokenType.rightBrace.toString()
        ));
        if (panic_mode && token != null && Lookup.Statement.contains(token.getType())) StatementList();
    }

    private void StatementOptional() throws SyntacticException {
        if (Lookup.Statement.contains(token.getType())) {
            Statement();
        } else if (token.getType() == TokenType.rightBrace ||
                   token.getType() == TokenType.kwCase ||
                   token.getType() == TokenType.kwDefault) {
            return;
        } else throwException(List.of(
            "a statement",
            TokenType.rightBrace.toString(),
            TokenType.kwCase.toString(),
            TokenType.kwDefault.toString()
        ));
    }

    private void Statement() throws SyntacticException {
        switch (token.getType()) {
            case idClass -> { // Local Var of a Class Type or a Static Method Call
                match(TokenType.idClass);
                StatementRest();
                match(TokenType.semicolon);
            }
            case semicolon -> match(TokenType.semicolon);
            case kwVar, kwBoolean, kwChar, kwInt, kwFloat -> {
                LocalVarPrimitiveType();
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
            case kwFor -> For();
            case kwWhile -> While();
            case kwSwitch -> Switch();
            case leftBrace -> Block();
            default -> {
                if (Lookup.Expression.contains(token.getType())) {
                    Expression();
                    match(TokenType.semicolon);
                }
                else throwException(List.of(
                    "a statement"
                ));
            }
        }
    }

   /*
    * Static MethodCall -> Access -> Operand -> Composite Expression -> Expression [Start of Expression]
    * <StatementRest> ::= . idMetVar <ActualArgs> <ChainedOptional> <CompositeExpressionRest> <ExpressionRest> ;
    *
    * LocalVar of a ClassType [Start of LocalVar]
    * <StatementRest> ::= <GenericTypeOptional> <IdMetVarList> <AssignmentOp> <CompositeExpression> ;
    */
    private void StatementRest() throws SyntacticException {
        switch (token.getType()) {
            case dot -> {
                match(TokenType.dot);
                match(TokenType.idMetVar);
                ActualArgs();
                ChainedOptional();
                CompositeExpressionRest();
                ExpressionRest();
            }
            case opLess, idMetVar -> {
                GenericTypeOptional();
                IdMetVarList();
                AssignmentOptional();
            }
            default -> throwException(List.of(
                "a static method call",
                "an assignment statement"
            ));
        }
    }

    private void LocalVarPrimitiveType() throws SyntacticException {
        switch (token.getType()) {
            case kwVar -> {
                match(TokenType.kwVar);
                match(TokenType.idMetVar);
                match(TokenType.opAssign);
                CompositeExpression();
            }
            case kwBoolean, kwChar, kwInt, kwFloat -> {
                PrimitiveType();
                IdMetVarList();
                AssignmentOptional();
            }
            default -> throwException(List.of(
                TokenType.kwVar.toString(),
                "a type"
            ));
        }
    }

    private void IdMetVarList() throws SyntacticException {
        match(TokenType.idMetVar);
        IdMetVarListRest();
    }

    private void IdMetVarListRest() throws SyntacticException {
        switch (token.getType()) {
            case comma -> {
                match(TokenType.comma);
                IdMetVarList();
            }
            case opAssign, semicolon -> {
                return;
            }
            default -> throwException(List.of(
                TokenType.opAssign.toString(),
                "a comma and another identifier",
                TokenType.semicolon.toString()
            ));
        }
    }

    private void AssignmentOptional() throws SyntacticException {
        switch (token.getType()) {
            case opAssign -> {
                match(TokenType.opAssign);
                CompositeExpression();
            }
            case semicolon -> {
                return;
            }
            default -> throwException(List.of(
                TokenType.opAssign.toString(),
                TokenType.semicolon.toString()
            ));
        }
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
        if (token.getType() == TokenType.kwElse) {
            match(TokenType.kwElse);
            Statement();
        } else if (Follow.Statement.contains(token.getType())) {
            return;
        } else {
            throwException(List.of(
                "an else statement",
                "another statement",
                TokenType.rightBrace.toString()
            ));
        }
    }

    private void For() throws SyntacticException {
        match(TokenType.kwFor);
        match(TokenType.leftParenthesis);
        ForRest();
    }

    private void ForRest() throws SyntacticException {
        switch (token.getType()) {
            case kwVar, idMetVar -> {
                VarOptional();
                match(TokenType.idMetVar);
                ForWithAssignment();
            }
            case kwBoolean, kwChar, kwInt, kwFloat -> {
                PrimitiveType();
                match(TokenType.idMetVar);
                ForWithAssignment();
            }
            case idClass -> {
                ClassType();
                match(TokenType.idMetVar);
                ForClass();
            }
            default -> throwException(List.of(
                TokenType.kwVar.toString(),
                "a type",
                TokenType.idMetVar.toString()
            ));
        }
    }

    private void ForClass() throws SyntacticException {
        switch (token.getType()) {
            case colon -> {
                match(TokenType.colon);
                CompositeExpression();
                match(TokenType.rightParenthesis);
                Statement();
            }
            case opAssign -> ForWithAssignment();
            default -> throwException(List.of(
                TokenType.colon.toString(),
                TokenType.opAssign.toString()
            ));
        }
    }

    private void ForWithAssignment() throws SyntacticException {
        match(TokenType.opAssign);
        CompositeExpression();
        match(TokenType.semicolon);
        CompositeExpression();
        match(TokenType.semicolon);
        Expression();
        match(TokenType.rightParenthesis);
        Statement();
    }

    private void VarOptional() throws SyntacticException {
        switch (token.getType()) {
            case kwVar -> match(TokenType.kwVar);
            case idMetVar -> {return;}
            default -> throwException(List.of(
                TokenType.kwVar.toString(),
                TokenType.idMetVar.toString()
            ));
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
            default -> throwException(List.of(
                TokenType.kwCase.toString(),
                TokenType.kwDefault.toString(),
                TokenType.rightBrace.toString()
            ));
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
            default -> throwException(List.of(
                TokenType.kwCase.toString(),
                TokenType.kwDefault.toString()
            ));
        }
    }

//------------------------------------------------------------------------------

    private void ExpressionOptional() throws SyntacticException {
        if (Lookup.Expression.contains(token.getType())) {
            Expression();
        } else if (token.getType() == TokenType.semicolon) {
            return;
        } else {
            throwException(List.of(
                "an expression",
                TokenType.semicolon.toString()
            ));
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
            default -> {
                if (Follow.Expression.contains(token.getType())) return;
                else throwException(List.of(
                    "an assignment operator",
                    TokenType.rightParenthesis.toString(),
                    TokenType.semicolon.toString(),
                    "a comma and another expression"
                ));
            }
        }
    }

    private void AssignmentOp() throws SyntacticException {
        switch (token.getType()) {
            case opAssign -> match(TokenType.opAssign);
            case opPlusAssign -> match(TokenType.opPlusAssign);
            case opMinusAssign -> match(TokenType.opMinusAssign);
            default -> throwException(List.of(
                TokenType.opAssign.toString(),
                TokenType.opPlusAssign.toString(),
                TokenType.opMinusAssign.toString())
            );
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
        } else if (Follow.CompositeExpression.contains(token.getType())) {
            return;
        } else {
            throwException(List.of(
                "a binary operator",
                "an assignment operator",
                TokenType.rightParenthesis.toString(),
                TokenType.semicolon.toString(),
                "a comma and another expression"
            ));
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
            default -> throwException(List.of(
            "a binary operator"
            ));
        }
    }

    private void BasicExpression() throws SyntacticException {
        if (token.getType() == TokenType.opPlus ||
            token.getType() == TokenType.opMinus ||
            token.getType() == TokenType.opNot) {

            UnaryOp();
            Operand();
        } else if (Lookup.Operand.contains(token.getType())) {
            Operand();
        } else {
            throwException(List.of(
                "a unary operator",
                "an operand"
            ));
        }
    }

    private void UnaryOp() throws SyntacticException {
        switch (token.getType()) {
            case opPlus -> match(TokenType.opPlus);
            case opMinus -> match(TokenType.opMinus);
            case opNot -> match(TokenType.opNot);
            default -> throwException(List.of(
                TokenType.opPlus.toString(),
                TokenType.opMinus.toString(),
                TokenType.opNot.toString()
            ));
        }
    }

    private void Operand() throws SyntacticException {
        if (Lookup.Literal.contains(token.getType())) {
            Literal();
        } else if (Lookup.Access.contains(token.getType())) {
            Access();
        } else {
            throwException(List.of(
                "a literal",
                "an access to an object"
            ));
        }
    }

    private void Literal() throws SyntacticException {
        if (Lookup.PrimitiveLiteral.contains(token.getType())) {
            PrimitiveLiteral();
        } else if (Lookup.ObjectLiteral.contains(token.getType())) {
            ObjectLiteral();
        } else {
            throwException(List.of(
                "a primitive literal",
                "an object literal"
            ));
        }
    }

    private void PrimitiveLiteral() throws SyntacticException {
        switch (token.getType()) {
            case trueLiteral -> match(TokenType.trueLiteral);
            case falseLiteral -> match(TokenType.falseLiteral);
            case intLiteral -> match(TokenType.intLiteral);
            case floatLiteral -> match(TokenType.floatLiteral);
            case charLiteral -> match(TokenType.charLiteral);
            default -> throwException(List.of(
                TokenType.trueLiteral.toString(),
                TokenType.falseLiteral.toString(),
                TokenType.intLiteral.toString(),
                TokenType.floatLiteral.toString(),
                TokenType.charLiteral.toString()
            ));
        }
    }

    private void ObjectLiteral() throws SyntacticException {
        switch (token.getType()) {
            case nullLiteral -> match(TokenType.nullLiteral);
            case stringLiteral -> match(TokenType.stringLiteral);
            default -> throwException(List.of(
                TokenType.nullLiteral.toString(),
                TokenType.stringLiteral.toString()
            ));
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
            case idClass -> StaticMethodAccess();
            case leftParenthesis -> ParenthesizedExpression();
            default -> throwException(List.of(
            "a reference to an object"
            ));
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
        if (token.getType() == TokenType.leftParenthesis) {
            ActualArgs();
        } else if (Follow.Primary.contains(token.getType())) {
            return;
        } else {
            throwException(List.of(
                TokenType.dot.toString(),
                "actual arguments",
                "a binary operator",
                "an assignment operator",
                TokenType.comma.toString(),
                TokenType.rightParenthesis.toString(),
                TokenType.semicolon.toString()
            ));
        }
    }

    private void ConstructorAccess() throws SyntacticException {
        match(TokenType.kwNew);
        match(TokenType.idClass);
        GenericTypeOptionalEmpty();
        ActualArgs();
    }

    private void StaticMethodAccess() throws SyntacticException {
        match(TokenType.idClass);
        match(TokenType.dot);
        match(TokenType.idMetVar);
        ActualArgs();
    }

    private void ParenthesizedExpression() throws SyntacticException {
        match(TokenType.leftParenthesis);
        Expression();
        match(TokenType.rightParenthesis);
    }

    private void GenericTypeOptionalEmpty() throws SyntacticException {
        if (token.getType() == TokenType.opLess) {
            match(TokenType.opLess);
            GenericListOptional();
            match(TokenType.opGreater);
        } else if (token.getType() == TokenType.leftParenthesis) {
            return;
        } else {
            throwException(List.of(
                TokenType.leftParenthesis.toString(),
                "a parameterized type instantiation"
            ));
        }
    }

    private void GenericListOptional() throws SyntacticException {
        if (token.getType() == TokenType.idClass) {
            GenericTypeList();
        } else if (token.getType() == TokenType.opGreater) {
            return;
        } else {
            throwException(List.of(
                TokenType.idClass.toString(),
                TokenType.opGreater.toString()
            ));
        }
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
            throwException(List.of(
                "a list of expressions",
                TokenType.rightParenthesis.toString()
            ));
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
            default -> throwException(List.of(
                "a comma and another expression",
                TokenType.rightParenthesis.toString()
            ));
        }
    }

    private void ChainedOptional() throws SyntacticException {
        if (token.getType() == TokenType.dot) {
            ChainedIdMetVar();
        } else if (Follow.BasicExpression.contains(token.getType())) {
            return;
        } else {
            throwException(List.of(
                TokenType.dot.toString(),
                "a binary operator",
                "an assignment operator",
                TokenType.comma.toString(),
                TokenType.rightParenthesis.toString(),
                TokenType.semicolon.toString()
            ));
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
            default -> {
                if (Follow.BasicExpression.contains(token.getType())) return;
                else throwException(List.of(
                    TokenType.dot.toString(),
                    "actual arguments",
                    "a binary operator",
                    "an assignment operator",
                    TokenType.comma.toString(),
                    TokenType.rightParenthesis.toString(),
                    TokenType.semicolon.toString()
                ));
            }
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

    private void saveError(String message) {
        if (token != null) {
            if (!errors.containsKey(token.getLine()))
                errors.put(token.getLine(), new Pair<>(new ArrayList<>(), ""));

            errors.get(token.getLine()).getFirst().add(new Error(
                message,
                token.getLexeme(),
                token.getLine(),
                token.getColumn(),
                ErrorType.Syntactic
            ));
        }
    }

    private void throwException(List<String> expected) throws SyntacticException {
        if (panic_mode) return;
        String message = Formater.expectedResult(expected, token);
        saveError(message);
        if (ParserConfig.CONTINUE_ON_ERROR) recoverFromError();
        else throw new SyntacticException(message);
    }

    private void recoverFromError() throws SyntacticException {
        panic_mode = true;
        while (token != null && token.getType() != TokenType.EOF) {
            if (Recovery.synchronize_set.contains(token.getType())) return;
            token = getToken();
        }
        throw new SyntacticException("");
    }

    private void consumeTokens() throws SyntacticException {
        while (token != null && token.getType() != TokenType.EOF) token = getToken();
    }
}