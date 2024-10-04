package main.java.parser;

import main.java.lexer.Lexer;
import main.java.utils.Formater;
import main.java.config.ParserConfig;
import main.java.config.SemanticConfig;

import main.java.model.Error;
import main.java.model.Pair;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.model.ErrorType;

import main.java.semantic.SymbolTable;
import main.java.semantic.entities.Class;
import main.java.semantic.entities.model.Type;
import main.java.semantic.entities.Attribute;
import main.java.semantic.entities.Parameter;
import main.java.semantic.entities.model.Unit;
import main.java.semantic.entities.Method;
import main.java.semantic.entities.Constructor;
import main.java.semantic.entities.AbstractMethod;
import main.java.semantic.entities.model.type.TypeVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.java.exeptions.LexicalException;
import main.java.exeptions.SyntacticException;

public class ParserImpl implements Parser {
    private final Map<Integer, Pair<List<Error>, String>> errors;
    private final Lexer lexer;
    private Token token;
    private boolean panic_mode;

    private Token entity_type_token;
    private Token entity_name_token;
    private final List<Token> entity_generic_types = new ArrayList<>();

    private boolean entity_is_static = false;
    private boolean entity_is_private = false;

    private Method actualMethod;
    private Constructor actualConstructor;
    private AbstractMethod actualAbstractMethod;
    private Unit actualUnit;

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

    private Token match(TokenType tokenType) throws SyntacticException {
        Token previous = token;
        if (token == null || token.getType() != tokenType)
            throwException(List.of(tokenType.toString()));
        else {
            token = getToken();
            if (panic_mode) panic_mode = false;
        }
        return previous;
    }

    private void Start() throws SyntacticException {
        ClassList();
        SymbolTable.saveEOF(match(TokenType.EOF));
    }

//------------------------------------------------------------------------------

    private void ClassList() throws SyntacticException {
        switch (token.getType()) {
            case kwClass -> {
                Class();
                ClassList();
            }
            case kwAbstract -> {
                AbstractClass();
                ClassList();
            }
            case EOF -> {
                return;
            }
            default -> throwException(List.of(
                TokenType.kwClass.toString(),
                TokenType.kwAbstract.toString(),
                TokenType.EOF.toString()
            ));
        }

        while (panic_mode && token.getType() != TokenType.EOF) {
            if (token.getType() == TokenType.leftBrace) Class();
            if (Lookup.ClassList.contains(token.getType())) ClassList();
            if (panic_mode && token.getType() != TokenType.EOF) {token = getToken(); recoverFromError();}
        }
    }

    private void Class() throws SyntacticException {
        reset_entity();
        match(TokenType.kwClass);
        setName(ClassType());
        createClass();
        InheritanceOptional();
        match(TokenType.leftBrace);
        if (!panic_mode) MemberList();
        match(TokenType.rightBrace);
    }

    private void AbstractClass() throws SyntacticException {
        reset_entity();
        match(TokenType.kwAbstract);
        match(TokenType.kwClass);
        setName(ClassType());
        createClass();
        setAbstractClass();
        InheritanceOptional();
        match(TokenType.leftBrace);
        if (!panic_mode) AbstractMemberList();
        match(TokenType.rightBrace);
    }

    private void InheritanceOptional() throws SyntacticException {
        switch (token.getType()) {
            case kwExtends -> {
                match(TokenType.kwExtends);
                setSuperType(ClassType());
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
            reset_entity();
            VisibilityOptional();
            Member();
            saveMember();
            MemberList();
        } else if (token.getType() == TokenType.rightBrace) {
            return;
        } else throwException(List.of(
            "a member",
            TokenType.rightBrace.toString()
        ));
        if (panic_mode) recover_member_list(false);
    }

    private void AbstractMemberList() throws SyntacticException {
        if (Lookup.Member.contains(token.getType()) || token.getType() == TokenType.kwAbstract) {
            reset_entity();
            VisibilityOptional();
            AbstractMember();
            saveMember();
            AbstractMemberList();
        } else if (token.getType() == TokenType.rightBrace) {
            return;
        } else throwException(List.of(
            "a member",
            TokenType.kwAbstract.toString(),
            TokenType.rightBrace.toString()
        ));
        if (panic_mode) recover_member_list(true);
    }

    private void recover_member_list(boolean is_abstract) throws SyntacticException {
        while (panic_mode && token.getType() != TokenType.EOF) {
            while (Lookup.Statement.contains(token.getType())) {
                if (token.getType() == TokenType.leftBrace) Block();
                else {StatementList(); match(TokenType.rightBrace);}
            }
            if (Lookup.Member.contains(token.getType()) || token.getType() == TokenType.kwAbstract) {
                if (is_abstract) AbstractMemberList();
                else MemberList();
            }
            if (Lookup.ClassList.contains(token.getType())) {
                ClassList();
            }
            if (panic_mode && token.getType() != TokenType.EOF) {
                token = getToken();
                recoverFromError();
            }
        }
    }

    private void Member() throws SyntacticException {
        switch (token.getType()) {
            case idClass -> {
                setName(match(TokenType.idClass));
                MaybeConstructor();
            }
            case kwStatic -> {
                match(TokenType.kwStatic);
                setStatic();
                setType(MemberType());
                setName(match(TokenType.idMetVar));
                MemberRest();
            }
            case kwVoid -> {
                setType(match(TokenType.kwVoid));
                setName(match(TokenType.idMetVar));
                MemberRest();
            }
            case kwBoolean, kwChar, kwInt, kwFloat -> {
                setType(PrimitiveType());
                setName(match(TokenType.idMetVar));
                MemberRest();
            }
            default -> throwException(List.of(
            "a member"
            ));
        }
    }

    private void AbstractMember() throws SyntacticException {
        if (token.getType() == TokenType.kwAbstract) {
            match(TokenType.kwAbstract);
            setType(MemberType());
            setName(match(TokenType.idMetVar));
            createAbstractMethod();
            FormalArgs();
            match(TokenType.semicolon);
        } else if (Lookup.Member.contains(token.getType())) {
            Member();
        } else throwException(List.of(
            TokenType.kwAbstract.toString(),
            "a member"
        ));
    }

    private void MaybeConstructor() throws SyntacticException {
        switch (token.getType()) {
            case leftParenthesis -> {
                createConstructor();
                FormalArgs();
                Block();
            }
            case opLess, idMetVar -> {
                GenericTypeOptional();
                setType(entity_name_token);
                setName(match(TokenType.idMetVar));
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
                createAttribute();
                AssignmentOptional();
                match(TokenType.semicolon);
            }
            case leftParenthesis -> {
                createMethod();
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

    private void VisibilityOptional() throws SyntacticException {
        switch (token.getType()) {
            case kwPublic -> match(TokenType.kwPublic);
            case kwPrivate -> {match(TokenType.kwPrivate); setPrivate();}
            default -> {
               if (Lookup.Member.contains(token.getType()) || token.getType() == TokenType.kwAbstract) return;
               else throwException(List.of(
                   TokenType.kwPublic.toString(),
                   TokenType.kwPrivate.toString(),
                   "a member"
               ));
            }
        }
    }

    private Token MemberType() throws SyntacticException {
        if (token.getType() == TokenType.kwVoid) {
            return match(TokenType.kwVoid);
        } else if (Lookup.Type.contains(token.getType())) {
            return Type();
        } else return throwException(List.of(
        "a type"
        ));
    }

    private Token Type() throws SyntacticException {
        return switch (token.getType()) {
            case kwBoolean, kwChar, kwInt, kwFloat -> PrimitiveType();
            case idClass -> ClassType();
            default -> throwException(List.of(
                "a primitive type",
                "a class type"
            ));
        };
    }

    private Token PrimitiveType() throws SyntacticException {
        return switch (token.getType()) {
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
        };
    }

    private Token ClassType() throws SyntacticException {
        Token token = match(TokenType.idClass);
        GenericTypeOptional();
        return token;
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
        entity_generic_types.add(match(TokenType.idClass));
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
        } else throwException(List.of(
            "a formal parameter list",
            TokenType.rightParenthesis.toString()
        ));
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
                TokenType.comma.toString(),
                TokenType.rightParenthesis.toString()
            ));
        }
    }

    private void FormalArg() throws SyntacticException {
        Token type = Type();
        Token name = match(TokenType.idMetVar);
        addParameter(type, name);
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
        if (panic_mode && Lookup.Statement.contains(token.getType())) StatementList();
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
                TokenType.comma.toString(),
                TokenType.opAssign.toString(),
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
        } else throwException(List.of(
            "an else statement",
            "another statement",
            TokenType.rightBrace.toString()
        ));
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
        } else throwException(List.of(
            "an expression",
            TokenType.semicolon.toString()
        ));
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
                    TokenType.comma.toString(),
                    TokenType.rightParenthesis.toString(),
                    TokenType.semicolon.toString()
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
        } else throwException(List.of(
            "a binary operator",
            "an assignment operator",
            TokenType.semicolon.toString(),
            TokenType.rightParenthesis.toString(),
            TokenType.comma.toString()
        ));
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
        } else throwException(List.of(
            "a unary operator",
            "an operand"
        ));
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
        } else throwException(List.of(
            "a literal",
            "an access to an object"
        ));
    }

    private void Literal() throws SyntacticException {
        if (Lookup.PrimitiveLiteral.contains(token.getType())) {
            PrimitiveLiteral();
        } else if (Lookup.ObjectLiteral.contains(token.getType())) {
            ObjectLiteral();
        } else throwException(List.of(
            "a primitive literal",
            "an object literal"
        ));
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
        } else throwException(List.of(
            TokenType.leftParenthesis.toString(),
            TokenType.dot.toString(),
            "an assignment operator",
            TokenType.semicolon.toString(),
            TokenType.rightParenthesis.toString(),
            TokenType.comma.toString(),
            "a binary operator"
        ));
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
        switch (token.getType()) {
            case opLess -> {
                match(TokenType.opLess);
                GenericListOptional();
                match(TokenType.opGreater);
            }
            case leftParenthesis -> {
                return;
            }
            default -> throwException(List.of(
                TokenType.leftParenthesis.toString(),
                "a parameterized type instantiation"
            ));
        }
    }

    private void GenericListOptional() throws SyntacticException {
        switch (token.getType()) {
            case idClass -> GenericTypeList();
            case opGreater -> {
                return;
            }
            default -> throwException(List.of(
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
        } else throwException(List.of(
            "a list of expressions",
            TokenType.rightParenthesis.toString()
        ));
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
                TokenType.comma.toString(),
                TokenType.rightParenthesis.toString()
            ));
        }
    }

    private void ChainedOptional() throws SyntacticException {
        if (token.getType() == TokenType.dot) {
            ChainedIdMetVar();
        } else if (Follow.BasicExpression.contains(token.getType())) {
            return;
        } else throwException(List.of(
            TokenType.dot.toString(),
            "an assignment operator",
            TokenType.semicolon.toString(),
            TokenType.rightParenthesis.toString(),
            TokenType.comma.toString(),
            "a binary operator"
        ));
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
                    TokenType.leftParenthesis.toString(),
                    "an assignment operator",
                    TokenType.semicolon.toString(),
                    TokenType.rightParenthesis.toString(),
                    TokenType.comma.toString(),
                    "a binary operator"
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
        if (token == null) return;

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

    private Token throwException(List<String> expected) throws SyntacticException {
        if (panic_mode) return null;
        String message = Formater.expectedResult(expected, token);
        saveError(message);
        if (ParserConfig.CONTINUE_ON_ERROR) recoverFromError();
        else throw new SyntacticException(message);
        return null;
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

//------------------------------------------------------------------------------

    private void createClass() {
        if (panic_mode) return;
        SymbolTable.addClass(
            entity_name_token.getLexeme(),
            new Class(
                entity_name_token.getLexeme(),
                entity_name_token,
                getGenericTypes()
            )
        );
        SymbolTable.actualClass = SymbolTable.getClass(entity_name_token.getLexeme());
    }

    private void setName(Token name) {
        entity_name_token = name;
    }

    private void setType(Token type) {
        entity_type_token = type;
    }

    private void setSuperType(Token super_token) {
        if (SymbolTable.actualClass == null || panic_mode) return;
        SymbolTable.actualClass.setSuperType(Type.createType(super_token, getGenericTypes()));
    }

    private void setStatic() {
        entity_is_static = true;
    }

    private void setPrivate() {
        entity_is_private = true;
    }

    private void setAbstractClass() {
        if (SymbolTable.actualClass == null || panic_mode) return;
        SymbolTable.actualClass.setAbstract();
    }

    private void createAttribute() {
        if (SymbolTable.actualClass == null || panic_mode) return;

        SymbolTable.actualClass.addAttribute(
            new Attribute(
                entity_name_token.getLexeme(),
                entity_name_token,
                Type.createType(entity_type_token, getGenericTypes()),
                entity_is_static,
                entity_is_private
            )
        );
    }

    private void createMethod() {
        if (panic_mode) return;
        actualUnit = actualMethod = new Method(
            withParameterSeparator(entity_name_token.getLexeme()),
            entity_name_token
        );
        actualUnit.setReturn(Type.createType(entity_type_token, getGenericTypes()));
        if (entity_is_private) actualUnit.setPrivate();
        if (entity_is_static) actualUnit.setStatic();
        SymbolTable.actualMethod = actualMethod;
    }

    private void createConstructor() {
        if (panic_mode) return;
        actualUnit = actualConstructor = new Constructor(
            withParameterSeparator(entity_name_token.getLexeme()),
            entity_name_token
        );
        if (entity_is_private) actualUnit.setPrivate();
        if (entity_is_static) actualUnit.setStatic();
        SymbolTable.actualConstructor = actualConstructor;
    }

    private void createAbstractMethod() {
        if (panic_mode) return;
        actualUnit = actualAbstractMethod = new AbstractMethod(
            withParameterSeparator(entity_name_token.getLexeme()),
            entity_name_token
        );
        actualUnit.setReturn(Type.createType(entity_type_token, getGenericTypes()));
        if (entity_is_private) actualUnit.setPrivate();
        if (entity_is_static) actualUnit.setStatic();
        SymbolTable.actualAbstractMethod = actualAbstractMethod;
    }

    private void addParameter(Token param_type_token, Token param_name_token) {
        if (actualUnit == null || panic_mode) return;
        actualUnit.addParameter(new Parameter(
            param_name_token.getLexeme(),
            param_name_token,
            Type.createType(param_type_token, getGenericTypes())
        ));
        actualUnit.setName(actualUnit.getName() + SemanticConfig.PARAMETER_TYPE_COUNTER);
    }

    private void saveMember() {
        if (SymbolTable.actualClass == null || actualUnit == null || panic_mode) return;

        if (actualUnit.getName().endsWith(SemanticConfig.PARAMETER_TYPE_SEPARATOR))
            actualUnit.setName(removeLastChar(actualUnit.getName()));

        if (actualMethod != null) {
            SymbolTable.actualClass.addMethod(actualMethod);
        } else if (actualConstructor != null) {
            SymbolTable.actualClass.addConstructor(actualConstructor);
        } else if (actualAbstractMethod != null) {
            SymbolTable.actualClass.addAbstractMethod(actualAbstractMethod);
        }
    }

    private String withParameterSeparator(String name) {
        return name + SemanticConfig.PARAMETER_TYPE_SEPARATOR;
    }

    private String removeLastChar(String string) {
        return string != null? string.substring(0, string.length()-1):null;
    }

    public List<TypeVar> getGenericTypes() {
        List<TypeVar> type_vars = new ArrayList<>();
        entity_generic_types.forEach(token -> type_vars.add(
            new TypeVar(
                token.getLexeme(),
                token
            )
        ));
        entity_generic_types.clear();
        return type_vars;
    }

    private void reset_entity() {
        entity_type_token = null;
        entity_name_token = null;
        entity_is_static = false;
        entity_is_private = false;
        actualMethod = null;
        actualConstructor = null;
        actualAbstractMethod = null;
        actualUnit = null;
        entity_generic_types.clear();
    }
}