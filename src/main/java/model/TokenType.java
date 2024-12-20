package main.java.model;

public enum TokenType {
    idMetVar ("a variable/method identifier"),
    idClass ("a class identifier"),
    intLiteral ("an int literal"),
    floatLiteral ("a float literal"),
    charLiteral ("a char literal"),
    stringLiteral ("a string literal"),
    trueLiteral ("true"),
    falseLiteral ("false"),
    nullLiteral ("null"),
    opGreater ("'>'"),
    opLess ("'<'"),
    opGreaterEqual ("'>='"),
    opLessEqual ("'<='"),
    opEqual ("'=='"),
    opNotEqual ("'!='"),
    opPlus ("'+'"),
    opPlusAssign ("'+='"),
    opMinus ("'-'"),
    opMinusAssign ("'-='"),
    opTimes ("'*'"),
    opDiv ("'/'"),
    opAnd ("'&&'"),
    opOr ("'||'"),
    opNot ("'!'"),
    opMod ("'%'"),
    kwClass ("class"),
    kwAbstract ("abstract"),
    kwBoolean ("boolean"),
    kwIf ("if"),
    kwSwitch ("switch"),
    kwThis ("this"),
    kwExtends ("extends"),
    kwChar ("char"),
    kwElse ("else"),
    kwCase ("case"),
    kwDefault ("default"),
    kwNew ("new"),
    kwPublic ("public"),
    kwPrivate ("private"),
    kwInt ("int"),
    kwFloat ("float"),
    kwWhile ("while"),
    kwFor ("for"),
    kwBreak ("break"),
    kwStatic ("static"),
    kwReturn ("return"),
    kwVoid ("void"),
    kwVar ("var"),
    EOF ("end of file"),
    colon ("':'"),
    semicolon ("';'"),
    comma ("','"),
    dot ("'.'"),
    leftParenthesis ("'('"),
    rightParenthesis ("')'"),
    leftBrace ("'{'"),
    rightBrace ("'}'"),
    opAssign ("'='"),
    kwSuper ("super");

    private final String name;

    TokenType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}