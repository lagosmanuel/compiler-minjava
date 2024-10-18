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

import main.java.semantic.entities.model.Statement;
import main.java.semantic.entities.model.statement.*;
import main.java.semantic.entities.model.statement.chained.*;
import main.java.semantic.entities.model.statement.expression.*;
import main.java.semantic.entities.model.statement.primary.*;
import main.java.semantic.entities.model.statement.switchs.*;

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

    private Block actualBlock;

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
        reset_actual_class();
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
        reset_actual_class();
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
                if (token.getType() == TokenType.leftBrace) actualUnit.setBody(Block());
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
                actualUnit.setBody(Block());
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
                actualUnit.setBody(Block());
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

    private Block Block() throws SyntacticException {
        Block block = new Block(match(TokenType.leftBrace));
        block.setParent(actualBlock);
        actualBlock = block;
        StatementList();
        match(TokenType.rightBrace);
        actualBlock = actualBlock.getParent();
        return block;
    }

    private void StatementList() throws SyntacticException {
        if (Lookup.Statement.contains(token.getType())) {
            Statement statement = Statement();
            if (actualBlock != null) actualBlock.addStatement(statement);
            StatementList();
        } else if (token.getType() == TokenType.rightBrace) {
            return;
        } else throwException(List.of(
            "a statement",
            TokenType.rightBrace.toString()
        ));
        if (panic_mode && Lookup.Statement.contains(token.getType())) StatementList();
    }

    private Statement StatementOptional() throws SyntacticException {
        if (Lookup.Statement.contains(token.getType())) {
            return Statement();
        } else if (token.getType() == TokenType.rightBrace ||
                   token.getType() == TokenType.kwCase ||
                   token.getType() == TokenType.kwDefault) {
            return null;
        } else throwException(List.of(
            "a statement",
            TokenType.rightBrace.toString(),
            TokenType.kwCase.toString(),
            TokenType.kwDefault.toString()
        ));
        return null;
    }

    private Statement Statement() throws SyntacticException {
        return switch (token.getType()) {
            case idClass -> { // Local Var of a Class Type or a Static Method Call
                Token identifier = match(TokenType.idClass);
                Statement statement = StatementRest(identifier);
                match(TokenType.semicolon);
                yield statement;
            }
            case semicolon -> new NOOP(match(TokenType.semicolon));
            case kwVar, kwBoolean, kwChar, kwInt, kwFloat -> {
                Statement statement = LocalVarPrimitiveType();
                match(TokenType.semicolon);
                yield statement;
            }
            case kwReturn -> {
                Statement statement = Return();
                match(TokenType.semicolon);
                yield statement;
            }
            case kwBreak -> {
                Statement statement = Break();
                match(TokenType.semicolon);
                yield statement;
            }
            case kwIf -> If();
            case kwFor -> For();
            case kwWhile -> While();
            case kwSwitch -> Switch();
            case leftBrace -> Block();
            default -> {
                if (Lookup.Expression.contains(token.getType())) {
                    Statement statement = Expression();
                    match(TokenType.semicolon);
                    yield statement;
                } else {
                    throwException(List.of(
                        "a statement"
                    ));
                    yield null;
                }
            }
        };
    }

   /*
    * Static MethodCall -> Access -> Operand -> Composite Expression -> Expression [Start of Expression]
    * <StatementRest> ::= . idMetVar <ActualArgs> <ChainedOptional> <CompositeExpressionRest> <ExpressionRest> ;
    *
    * LocalVar of a ClassType [Start of LocalVar]
    * <StatementRest> ::= <GenericTypeOptional> <IdMetVarList> <AssignmentOp> <CompositeExpression> ;
    */
    private Statement StatementRest(Token identifier) throws SyntacticException {
        return switch (token.getType()) {
            case dot -> {
                match(TokenType.dot);
                StaticMethodAccess access = new StaticMethodAccess(
                    identifier,
                    match(TokenType.idMetVar),
                    ActualArgs()
                );
                access.setChained(ChainedOptional());
                yield ExpressionRest(CompositeExpressionRest(access));
            }
            case opLess, idMetVar -> {
                GenericTypeOptional();
                yield new LocalVar(
                    Type.createType(identifier, getGenericTypes()),
                    IdMetVarList(new ArrayList<>()),
                    AssignmentOptional()
                );
            }
            default -> {
                throwException(List.of(
                    "a static method call",
                    "an assignment statement"
                ));
                yield null;
            }
        };
    }

    private Statement LocalVarPrimitiveType() throws SyntacticException {
        return switch (token.getType()) {
            case kwVar -> {
                Type type = Type.createType(match(TokenType.kwVar), null);
                Token identifier = match(TokenType.idMetVar);
                match(TokenType.opAssign);
                Expression expression = CompositeExpression();
                yield new LocalVar(type, List.of(identifier), expression);
            }
            case kwBoolean, kwChar, kwInt, kwFloat -> new LocalVar(
                Type.createType(PrimitiveType(), null),
                IdMetVarList(new ArrayList<>()),
                AssignmentOptional()
            );
            default -> {
                throwException(List.of(
                    TokenType.kwVar.toString(),
                    "a type"
                ));
                yield null;
            }
        };
    }

    private List<Token> IdMetVarList(List<Token> identifiers) throws SyntacticException {
        identifiers.add(match(TokenType.idMetVar));
        IdMetVarListRest(identifiers);
        return identifiers;
    }

    private void IdMetVarListRest(List<Token> identifiers) throws SyntacticException {
        switch (token.getType()) {
            case comma -> {
                match(TokenType.comma);
                IdMetVarList(identifiers);
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

    private Expression AssignmentOptional() throws SyntacticException {
        return switch (token.getType()) {
            case opAssign -> {
                match(TokenType.opAssign);
                yield CompositeExpression();
            }
            case semicolon -> {
                yield null;
            }
            default -> {
                throwException(List.of(
                    TokenType.opAssign.toString(),
                    TokenType.semicolon.toString()
                ));
                yield null;
            }
        };
    }

    private Statement Return() throws SyntacticException {
        return new Return(match(TokenType.kwReturn), ExpressionOptional());
    }

    private Statement Break() throws SyntacticException {
        return new Break(match(TokenType.kwBreak));
    }

    private Statement If() throws SyntacticException {
        Token identifier = match(TokenType.kwIf);
        match(TokenType.leftParenthesis);
        Expression condition = Expression();
        match(TokenType.rightParenthesis);
        Statement then = Statement();
        return IfRest(identifier, condition, then);
    }

    private Statement IfRest(Token identifier, Expression condition, Statement then) throws SyntacticException {
        if (token.getType() == TokenType.kwElse) {
            match(TokenType.kwElse);
            return new IfElse(
                identifier,
                condition,
                then,
                Statement()
            );
        } else if (Follow.Statement.contains(token.getType())) {
            return new If(
                identifier,
                condition,
                then
            );
        } else throwException(List.of(
            "an else statement",
            "another statement",
            TokenType.rightBrace.toString()
        ));
        return null;
    }

    private Statement For() throws SyntacticException {
        Token identifier = match(TokenType.kwFor);
        match(TokenType.leftParenthesis);
        return ForRest(identifier);
    }

    private Statement ForRest(Token identifier) throws SyntacticException {
        return switch (token.getType()) {
            case kwVar, idMetVar -> {
                Type type = Type.createType(VarOptional(), null);
                Token it = match(TokenType.idMetVar);
                yield ForWithAssignment(identifier, type, it);
            }
            case kwBoolean, kwChar, kwInt, kwFloat -> {
                Type type = Type.createType(PrimitiveType(), null);
                Token it = match(TokenType.idMetVar);
                yield ForWithAssignment(identifier, type, it);
            }
            case idClass -> {
                Type type = Type.createType(ClassType(), getGenericTypes());
                Token it = match(TokenType.idMetVar);
                yield ForClass(identifier, type, it);
            }
            default -> {
                throwException(List.of(
                    TokenType.kwVar.toString(),
                    "a type",
                    TokenType.idMetVar.toString()
                ));
                yield null;
            }
        };
    }

    private Statement ForClass(Token identifier, Type type, Token it) throws SyntacticException {
        return switch (token.getType()) {
            case colon -> {
                match(TokenType.colon);
                CompositeExpression iterable = CompositeExpression();
                match(TokenType.rightParenthesis);
                Statement statement = Statement();
                yield new ForEach(identifier, new LocalVar(type, List.of(it)), iterable, statement);
            }
            case opAssign -> ForWithAssignment(identifier, type, it);
            default -> {
                throwException(List.of(
                    TokenType.colon.toString(),
                    TokenType.opAssign.toString()
                ));
                yield null;
            }
        };
    }

    private Statement ForWithAssignment(Token identifier, Type type, Token it) throws SyntacticException {
        Token operator = match(TokenType.opAssign);
        CompositeExpression value = CompositeExpression();
        match(TokenType.semicolon);
        CompositeExpression condition = CompositeExpression();
        match(TokenType.semicolon);
        Expression assignment = Expression();
        match(TokenType.rightParenthesis);
        Statement statement = Statement();
        return new For(
            identifier,
            type == null?
                new Assignment(new VarAccess(it), value, operator):
                new LocalVar(type, List.of(it), value),
            condition,
            assignment,
            statement
        );
    }

    private Token VarOptional() throws SyntacticException {
        return switch (token.getType()) {
            case kwVar -> match(TokenType.kwVar);
            case idMetVar -> {yield null;}
            default -> throwException(List.of(
                TokenType.kwVar.toString(),
                TokenType.idMetVar.toString()
            ));
        };
    }

    private Statement While() throws SyntacticException {
        Token identifier = match(TokenType.kwWhile);
        match(TokenType.leftParenthesis);
        Expression condition = Expression();
        match(TokenType.rightParenthesis);
        Statement statement = Statement();
        return new While(identifier, condition, statement);
    }

    private Statement Switch() throws SyntacticException {
        List<SwitchStatement> statements = new ArrayList<>();
        Token identifier = match(TokenType.kwSwitch);
        match(TokenType.leftParenthesis);
        Expression expression = Expression();
        match(TokenType.rightParenthesis);
        match(TokenType.leftBrace);
        SwitchStatementList(statements);
        match(TokenType.rightBrace);
        return new Switch(identifier, expression, statements);
    }

    private void SwitchStatementList(List<SwitchStatement> statements) throws SyntacticException {
        switch (token.getType()) {
            case kwCase, kwDefault -> {
                statements.add(SwitchStatement());
                SwitchStatementList(statements);
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

    private SwitchStatement SwitchStatement() throws SyntacticException {
        return switch (token.getType()) {
            case kwCase -> {
                Token identifier = match(TokenType.kwCase);
                Literal literal = Literal();
                match(TokenType.colon);
                Statement statement = StatementOptional();
                yield new Case(identifier, literal, statement);
            }
            case kwDefault -> {
                Token identifier = match(TokenType.kwDefault);
                match(TokenType.colon);
                Statement statement = Statement();
                yield new Default(identifier, statement);
            }
            default -> {
                throwException(List.of(
                    TokenType.kwCase.toString(),
                    TokenType.kwDefault.toString()
                ));
                yield null;
            }
        };
    }

//------------------------------------------------------------------------------

    private Expression ExpressionOptional() throws SyntacticException {
        if (Lookup.Expression.contains(token.getType())) {
            return Expression();
        } else if (token.getType() == TokenType.semicolon) {
            return null;
        } else throwException(List.of(
            "an expression",
            TokenType.semicolon.toString()
        ));
        return null;
    }

    private Expression Expression() throws SyntacticException {
        return ExpressionRest(CompositeExpression());
    }

    private Expression ExpressionRest(CompositeExpression expression) throws SyntacticException {
        switch (token.getType()) {
            case opAssign, opPlusAssign, opMinusAssign -> {
                Token operator = AssignmentOp();
                CompositeExpression right = CompositeExpression();
                return new Assignment(expression, right, operator);
            }
            default -> {
                if (Follow.Expression.contains(token.getType())) {
                    return expression;
                } else {
                    throwException(List.of(
                        "an assignment operator",
                        TokenType.comma.toString(),
                        TokenType.rightParenthesis.toString(),
                        TokenType.semicolon.toString()
                    ));
                    return null;
                }
            }
        }
    }

    private Token AssignmentOp() throws SyntacticException {
        return switch (token.getType()) {
            case opAssign -> match(TokenType.opAssign);
            case opPlusAssign -> match(TokenType.opPlusAssign);
            case opMinusAssign -> match(TokenType.opMinusAssign);
            default -> throwException(List.of(
                TokenType.opAssign.toString(),
                TokenType.opPlusAssign.toString(),
                TokenType.opMinusAssign.toString())
            );
        };
    }

    private CompositeExpression CompositeExpression() throws SyntacticException {
        return CompositeExpressionRest(BasicExpression());
    }

    private CompositeExpression CompositeExpressionRest(CompositeExpression expression) throws SyntacticException {
        if (Lookup.BinaryOp.contains(token.getType())) {
            Token operator = BinaryOp();
            BasicExpression right = BasicExpression();
            return CompositeExpressionRest(new BinaryExpression(expression, right, operator));
        } else if (Follow.CompositeExpression.contains(token.getType())) {
            return expression;
        } else throwException(List.of(
            "a binary operator",
            "an assignment operator",
            TokenType.semicolon.toString(),
            TokenType.rightParenthesis.toString(),
            TokenType.comma.toString()
        ));
        return null;
    }

    private Token BinaryOp() throws SyntacticException {
        return switch (token.getType()) {
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
        };
    }

    private BasicExpression BasicExpression() throws SyntacticException {
        if (token.getType() == TokenType.opPlus ||
            token.getType() == TokenType.opMinus ||
            token.getType() == TokenType.opNot) {

            Token operator = UnaryOp();
            Operand operand = Operand();
            return new UnaryExpression(operand, operator);
        } else if (Lookup.Operand.contains(token.getType())) {
            return Operand();
        } else throwException(List.of(
            "a unary operator",
            "an operand"
        ));
        return null;
    }

    private Token UnaryOp() throws SyntacticException {
        return switch (token.getType()) {
            case opPlus -> match(TokenType.opPlus);
            case opMinus -> match(TokenType.opMinus);
            case opNot -> match(TokenType.opNot);
            default -> throwException(List.of(
                TokenType.opPlus.toString(),
                TokenType.opMinus.toString(),
                TokenType.opNot.toString()
            ));
        };
    }

    private Operand Operand() throws SyntacticException {
        if (Lookup.Literal.contains(token.getType())) {
            return Literal();
        } else if (Lookup.Access.contains(token.getType())) {
            return Access();
        } else throwException(List.of(
            "a literal",
            "an access to an object"
        ));
        return null;
    }

    private Literal Literal() throws SyntacticException {
        if (Lookup.PrimitiveLiteral.contains(token.getType())) {
            return PrimitiveLiteral();
        } else if (Lookup.ObjectLiteral.contains(token.getType())) {
            return ObjectLiteral();
        } else throwException(List.of(
            "a primitive literal",
            "an object literal"
        ));
        return null;
    }

    private Literal PrimitiveLiteral() throws SyntacticException {
        return switch (token.getType()) {
            case trueLiteral -> new Literal(match(TokenType.trueLiteral));
            case falseLiteral -> new Literal(match(TokenType.falseLiteral));
            case intLiteral -> new Literal(match(TokenType.intLiteral));
            case floatLiteral -> new Literal(match(TokenType.floatLiteral));
            case charLiteral -> new Literal(match(TokenType.charLiteral));
            default -> {
                throwException(List.of(
                    TokenType.trueLiteral.toString(),
                    TokenType.falseLiteral.toString(),
                    TokenType.intLiteral.toString(),
                    TokenType.floatLiteral.toString(),
                    TokenType.charLiteral.toString()
                ));
                yield null;
            }
        };
    }

    private Literal ObjectLiteral() throws SyntacticException {
        return switch (token.getType()) {
            case nullLiteral -> new Literal(match(TokenType.nullLiteral));
            case stringLiteral -> new Literal(match(TokenType.stringLiteral));
            default -> {
                throwException(List.of(
                    TokenType.nullLiteral.toString(),
                    TokenType.stringLiteral.toString()
                ));
                yield null;
            }
        };
    }

    private Access Access() throws SyntacticException {
        Access access = Primary();
        Chained chained = ChainedOptional();
        if (access != null) access.setChained(chained);
        return access;
    }

    private Access Primary() throws SyntacticException {
        return switch (token.getType()) {
            case kwThis -> ThisAccess();
            case kwSuper -> SuperAccess();
            case idMetVar -> IdMetVarAccess();
            case kwNew -> ConstructorAccess();
            case idClass -> StaticMethodAccess();
            case leftParenthesis -> ParenthesizedExpression();
            default -> {
                throwException(List.of(
                    "a reference to an object"
                ));
                yield null;
            }
        };
    }

    private ThisAccess ThisAccess() throws SyntacticException {
        return new ThisAccess(match(TokenType.kwThis));
    }

    private SuperAccess SuperAccess() throws SyntacticException {
        return new SuperAccess(
            match(TokenType.kwSuper),
            SymbolTable.actualClass != null? SymbolTable.actualClass.getToken():null,
            ActualArgsOptional()
        );
    }

    private Access IdMetVarAccess() throws SyntacticException {
        return IdMetVarAccessRest(match(TokenType.idMetVar));
    }

    private Access IdMetVarAccessRest(Token identifier) throws SyntacticException {
        if (token.getType() == TokenType.leftParenthesis) {
            return new MethodAccess(identifier, ActualArgs());
        } else if (Follow.Primary.contains(token.getType())) {
            return new VarAccess(identifier);
        } else throwException(List.of(
            TokenType.leftParenthesis.toString(),
            TokenType.dot.toString(),
            "an assignment operator",
            TokenType.semicolon.toString(),
            TokenType.rightParenthesis.toString(),
            TokenType.comma.toString(),
            "a binary operator"
        ));
        return null;
    }

    private ConstuctorAccess ConstructorAccess() throws SyntacticException {
        match(TokenType.kwNew);
        Token identifier = match(TokenType.idClass);
        GenericTypeOptionalEmpty();
        List<Expression> actualArgs = ActualArgs();
        return new ConstuctorAccess(identifier, actualArgs);
    }

    private StaticMethodAccess StaticMethodAccess() throws SyntacticException {
        Token classId = match(TokenType.idClass);
        match(TokenType.dot);
        Token methodId = match(TokenType.idMetVar);
        List<Expression> actualArgs = ActualArgs();
        return new StaticMethodAccess(classId, methodId, actualArgs);
    }

    private ParenthesizedExpression ParenthesizedExpression() throws SyntacticException {
        Token identifier = match(TokenType.leftParenthesis);
        Expression expression = Expression();
        match(TokenType.rightParenthesis);
        return new ParenthesizedExpression(identifier, expression);
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

    private List<Expression> ActualArgsOptional() throws SyntacticException {
        if (token.getType() == TokenType.leftParenthesis) {
            return ActualArgs();
        } else if (Follow.Primary.contains(token.getType())) {
            return null;
        } else throwException(List.of(
            TokenType.leftParenthesis.toString(),
            TokenType.dot.toString(),
            "an assignment operator",
            TokenType.semicolon.toString(),
            TokenType.rightParenthesis.toString(),
            TokenType.comma.toString(),
            "a binary operator"
        ));
        return null;
    }

    private List<Expression> ActualArgs() throws SyntacticException {
        List<Expression> actualArgs = new ArrayList<>();
        match(TokenType.leftParenthesis);
        ExpressionListOptional(actualArgs);
        match(TokenType.rightParenthesis);
        return actualArgs;
    }

    private void ExpressionListOptional(List<Expression> actualArgs) throws SyntacticException {
        if (Lookup.Expression.contains(token.getType())) {
            ExpressionList(actualArgs);
        } else if (token.getType() == TokenType.rightParenthesis) {
            return;
        } else throwException(List.of(
            "a list of expressions",
            TokenType.rightParenthesis.toString()
        ));
    }

    private void ExpressionList(List<Expression> actualArgs) throws SyntacticException {
        actualArgs.add(Expression());
        ExpressionListRest(actualArgs);
    }

    private void ExpressionListRest(List<Expression> actualArgs) throws SyntacticException {
        switch (token.getType()) {
            case comma -> {
                match(TokenType.comma);
                ExpressionList(actualArgs);
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

    private Chained ChainedOptional() throws SyntacticException {
        if (token.getType() == TokenType.dot) {
            return ChainedIdMetVar();
        } else if (Follow.BasicExpression.contains(token.getType())) {
            return null;
        } else throwException(List.of(
            TokenType.dot.toString(),
            "an assignment operator",
            TokenType.semicolon.toString(),
            TokenType.rightParenthesis.toString(),
            TokenType.comma.toString(),
            "a binary operator"
        ));
        return null;
    }

    private Chained ChainedIdMetVar() throws SyntacticException {
        match(TokenType.dot);
        return ChainedIdMetVarRest(match(TokenType.idMetVar));
    }

    private Chained ChainedIdMetVarRest(Token identifier) throws SyntacticException {
        return switch (token.getType()) {
            case dot -> new ChainedVar(identifier, ChainedOptional());
            case leftParenthesis -> {
                List<Expression> actualArgs = ActualArgs();
                Chained chained = ChainedOptional();
                yield new ChainedMethod(identifier, actualArgs, chained);
            }
            default -> {
                if (Follow.BasicExpression.contains(token.getType())) {
                    yield new ChainedVar(identifier, null);
                } else {
                    throwException(List.of(
                        TokenType.dot.toString(),
                        TokenType.leftParenthesis.toString(),
                        "an assignment operator",
                        TokenType.semicolon.toString(),
                        TokenType.rightParenthesis.toString(),
                        TokenType.comma.toString(),
                        "a binary operator"
                    ));
                    yield null;
                }
            }
        };
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
        if (entity_name_token == null || panic_mode) return;
        Class newClass = new Class(
            entity_name_token.getLexeme(),
            entity_name_token,
            getGenericTypes()
        );
        SymbolTable.addClass(newClass);
        SymbolTable.actualClass = newClass;
    }

    private void setName(Token name) {
        entity_name_token = name;
    }

    private void setType(Token type) {
        entity_type_token = type;
    }

    private void setSuperType(Token super_token) {
        if (SymbolTable.actualClass == null || super_token == null || panic_mode) return;
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
        if (SymbolTable.actualClass == null || entity_name_token == null || entity_type_token == null || panic_mode)
            return;

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
        if (entity_name_token == null || entity_type_token == null || panic_mode) return;
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
        if (entity_name_token == null || panic_mode) return;
        actualUnit = actualConstructor = new Constructor(
            withParameterSeparator(entity_name_token.getLexeme()),
            entity_name_token
        );
        if (entity_is_private) actualUnit.setPrivate();
        if (entity_is_static) actualUnit.setStatic();
        SymbolTable.actualConstructor = actualConstructor;
    }

    private void createAbstractMethod() {
        if (entity_name_token == null || entity_type_token == null || panic_mode) return;
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
        if (param_type_token == null || param_name_token == null || actualUnit == null || panic_mode) return;
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
        return string != null && !string.isEmpty()? string.substring(0, string.length()-1):"";
    }

    public List<TypeVar> getGenericTypes() {
        List<TypeVar> type_vars = new ArrayList<>();
        int i = 0;
        for (Token token:entity_generic_types) type_vars.add(
            new TypeVar(
                token.getLexeme(),
                token,
                null,
                getGenericTypePositionOrDefault(token.getLexeme(), i++)
            )
        );
        entity_generic_types.clear();
        return type_vars;
    }

    private int getGenericTypePositionOrDefault(String type_name, int actual_position) {
        return SymbolTable.actualClass != null && SymbolTable.actualClass.hasTypeParameter(type_name)?
            SymbolTable.actualClass.getTypeParameter(type_name).getPosition():actual_position;
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

    private void reset_actual_class() {
        SymbolTable.actualClass = null;
    }
}