package main.java.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.java.config.LexerConfig;
import main.java.model.Error;
import main.java.model.Pair;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.model.ErrorType;
import main.java.utils.sourcemanager.SourceManager;
import main.java.messages.LexErrorMessages;
import main.java.messages.TokenMessages;
import main.java.exeptions.LexicalException;

import java.io.IOException;

public class LexerImpl implements Lexer {
    private final SourceManager sourceManager;
    private final Map<Integer, Pair<List<Error>, String>> errors;
    private String lexeme;
    private char ch;
    private int line;
    private int column;
    private boolean started;

    public LexerImpl(SourceManager sourceManager, Map<Integer, Pair<List<Error>, String>> errors) {
        this.sourceManager = sourceManager;
        this.lexeme = "";
        this.started = false;
        this.errors = errors;
    }

    public Token nextToken() throws LexicalException {
        if (!started) {
            ch = readChar();
            started = true;
        }
        return start();
    }

    private char readChar() {
        try {
            return sourceManager.getNextChar();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Token start() throws LexicalException {
        lexeme = "";
        appendCharLexeme(ch);
        line = sourceManager.getLineNumber();
        column = sourceManager.getColumnNumber();
        Token token = null;

        if (Character.isLowerCase(ch)) {
            return idMetVar();
        } else if (Character.isUpperCase(ch)) {
            return idClassVar();
        } else if (ch == '\'') {
            return openCharLiteral();
        } else if (ch == '"'){
            return closeStringLiteral();
        } else if (Character.isDigit(ch)) {
            return digit();
        } else if (ch == '(') {
            token = new Token(TokenType.leftParenthesis, lexeme, line, column);
        } else if (ch == ')') {
            token = new Token(TokenType.rightParenthesis, lexeme, line, column);
        } else if (ch == '{') {
            token = new Token(TokenType.leftBrace, lexeme, line, column);
        } else if (ch == '}') {
            token = new Token(TokenType.rightBrace, lexeme, line, column);
        } else if (ch == ';') {
            token = new Token(TokenType.semicolon, lexeme, line, column);
        } else if (ch == ',') {
            token = new Token(TokenType.comma, lexeme, line, column);
        } else if (ch == '.') {
            return openDot();
        } else if (ch == ':') {
            token = new Token(TokenType.colon, lexeme, line, column);
        } else if (ch == '>') {
            return closeGreaterOp();
        } else if (ch == '<') {
            return closeLessOp();
        } else if (ch == '=') {
            return closeEqualOp();
        } else if (ch == '+') {
            return closePlusOp();
        } else if (ch == '-') {
            return closeMinusOp();
        } else if (ch == '*') {
            token = new Token(TokenType.opTimes, lexeme, line, column);
        } else if (ch == '/') {
            return openComment();
        } else if (ch == '%') {
            token = new Token(TokenType.opMod, lexeme, line, column);
        } else if (ch == '&') {
            return closeAndOp();
        } else if (ch == '|') {
            return closeOrOp();
        } else if (ch == '!') {
            return closeNotOp();
        } else if (ch == SourceManager.NEWLINE) {
            saveLineIfError(line);
            do {
                ch = readChar();
            } while (ch == SourceManager.NEWLINE);
            return start();
        } else if (ch == SourceManager.END_OF_FILE) {
            saveLineIfError(line);
            token = new Token(TokenType.EOF, TokenMessages.EOF, line, column);
        }  else if (Character.isWhitespace(ch)) {
            do {
                if (ch == SourceManager.NEWLINE) saveLineIfError(line++);
                ch = readChar();
            } while (Character.isWhitespace(ch));
            return start();
        } else {
            restart();
            throwException(LexErrorMessages.ILLEGAL_CHAR);
        }

        restart();
        return token;
    }

    private Token idMetVar() {
        ch = readChar();

        if (Character.isLetterOrDigit(ch) || ch == '_') {
            appendCharLexeme(ch);
            return idMetVar();
        } else {
            return new Token(
                LexerConfig.RESERVED_WORDS.getOrDefault(lexeme, TokenType.idMetVar),
                lexeme,
                line,
                column
            );
        }
    }

    private Token idClassVar() {
        ch = readChar();

        if (Character.isLetterOrDigit(ch) || ch == '_') {
            appendCharLexeme(ch);
            return idClassVar();
        } else {
            return new Token(TokenType.idClass, lexeme, line, column);
        }
    }

    private Token digit() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return digit();
        } else if (ch == '.') {
            appendCharLexeme(ch);
            return openFloatDot();
        } else if (ch == 'e' || ch == 'E') {
            appendCharLexeme(ch);
            return openFloatExp();
        } else {
            checkInteger();
            return new Token(TokenType.intLiteral, lexeme, line, column);
        }
    }

    private Token openCharLiteral() throws LexicalException {
        ch = readChar();

        if (ch == '\'') {
            appendCharLexeme(ch);
            restart();
            throwException(LexErrorMessages.LITERAL_CHAR_EMPTY);
        } else if (ch == '\\') {
            appendCharLexeme(ch);
            return escapeCharLiteral();
        } else if (ch == SourceManager.NEWLINE) {
            throwException(LexErrorMessages.LITERAL_CHAR_NOT_CLOSED);
        } else if (ch == SourceManager.END_OF_FILE) {
            throwException(LexErrorMessages.LITERAL_CHAR_NOT_CLOSED);
        } else {
            appendCharLexeme(ch);
            return closeCharLiteral();
        }

        return null;
    }

    private Token escapeCharLiteral() throws LexicalException {
        ch = readChar();

        if (ch == SourceManager.NEWLINE || ch == SourceManager.END_OF_FILE) {
            throwException(LexErrorMessages.LITERAL_CHAR_BAD_ESCAPED);
        }

        appendCharLexeme(ch);
        return closeCharLiteral();
    }

    private Token closeCharLiteral() throws LexicalException {
        ch = readChar();

        if (ch == '\'') {
            restart();
            appendCharLexeme(ch);
            return new Token(TokenType.charLiteral, lexeme, line, column);
        } else {
            throwException(LexErrorMessages.LITERAL_CHAR_NOT_CLOSED);
        }

        return null;
    }

    private Token closeStringLiteral() throws LexicalException {
        ch = readChar();

        if (ch == '"') {
            appendCharLexeme(ch);
            restart();
            return new Token(TokenType.stringLiteral, lexeme, line, column);
        } else if (ch == SourceManager.NEWLINE) {
            throwException(LexErrorMessages.LITERAL_STR_NOT_CLOSED);
        } else if (ch == SourceManager.END_OF_FILE) {
            throwException(LexErrorMessages.LITERAL_STR_NOT_CLOSED);
        } else if (ch == '\\') {
            appendCharLexeme(ch);
            return escapeStringLiteral();
        } else {
            appendCharLexeme(ch);
            return closeStringLiteral();
        }

        return null;
    }

    private Token escapeStringLiteral() throws LexicalException {
        ch = readChar();

        if (ch == SourceManager.NEWLINE) {
            throwException(LexErrorMessages.LITERAL_STR_BAD_ESCAPED);
        } else if (ch == SourceManager.END_OF_FILE) {
            throwException(LexErrorMessages.LITERAL_STR_BAD_ESCAPED);
        }

        appendCharLexeme(ch);
        return closeStringLiteral();
    }

    private Token closeAndOp() throws LexicalException {
        ch = readChar();

        if (ch == '&') {
            restart();
            appendCharLexeme(ch);
            return new Token(TokenType.opAnd, lexeme, line, column);
        } else {
            throwException(LexErrorMessages.OP_AND_INVALID);
        }

        return null;
    }

    private Token closeOrOp() throws LexicalException {
        ch = readChar();

        if (ch == '|') {
            restart();
            appendCharLexeme(ch);
            return new Token(TokenType.opOr, lexeme, line, column);
        } else {
            throwException(LexErrorMessages.OP_OR_INVALID);
        }

        return null;
    }

    private Token openComment() throws LexicalException {
        ch = readChar();

        if (ch == '/') {
            appendCharLexeme(ch);
            return lineComment();
        } else if (ch == '*') {
            appendCharLexeme(ch);
            return blockComment();
        } else {
            return new Token(TokenType.opDiv, lexeme, line, column);
        }
    }

    private Token closeGreaterOp() {
        ch = readChar();

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            return new Token(TokenType.opGreaterEqual, lexeme, line, column);
        } else {
            return new Token(TokenType.opGreater, lexeme, line, column);
        }
    }

    private Token closeLessOp() {
        ch = readChar();

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            return new Token(TokenType.opLessEqual, lexeme, line, column);
        } else {
            return new Token(TokenType.opLess, lexeme, line, column);
        }
    }

    private Token closeEqualOp() {
        ch = readChar();

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            return new Token(TokenType.opEqual, lexeme, line, column);
        } else {
            return new Token(TokenType.opAssign, lexeme, line, column);
        }
    }

    private Token closePlusOp() {
        ch = readChar();

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            return new Token(TokenType.opPlusAssign, lexeme, line, column);
        } else {
            return new Token(TokenType.opPlus, lexeme, line, column);
        }
    }

    private Token closeMinusOp() {
        ch = readChar();

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            return new Token(TokenType.opMinusAssign, lexeme, line, column);
        } else {
            return new Token(TokenType.opMinus, lexeme, line, column);
        }
    }

    private Token lineComment() throws LexicalException {
        lexeme = "";

        do {
            ch = readChar();
        } while (ch != SourceManager.NEWLINE && ch != SourceManager.END_OF_FILE);

        return start();
    }

    private Token closeNotOp() {
        ch = readChar();

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            return new Token(TokenType.opNotEqual, lexeme, line, column);
        } else {
            return new Token(TokenType.opNot, lexeme, line, column);
        }
    }

    private Token blockComment() throws LexicalException {
        lexeme = "";
        boolean hasSeenAsterisk = false;

        do {
            ch = readChar();

            if (ch == '*') {
                hasSeenAsterisk = true;
            } else if (hasSeenAsterisk && ch == '/') {
                ch = readChar();
                return start();
            } else {
                hasSeenAsterisk = false;
            }
        } while (ch != SourceManager.END_OF_FILE);

        throwException(LexErrorMessages.COMMENT_BLOCK_NOT_CLOSED);
        return null;
    }

    private Token openDot() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return openDotFloat();
        } else {
            return new Token(TokenType.dot, lexeme, line, column);
        }
    }

    private Token openDotFloat() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return openDotFloat();
        } else if (ch == 'e' || ch == 'E') {
            appendCharLexeme(ch);
            return openDotFloatExp();
        } else {
            return new Token(TokenType.floatLiteral, lexeme, line, column);
        }
    }

    private Token openDotFloatExp() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return closeFloat();
        } else if (ch == '+' || ch == '-') {
            appendCharLexeme(ch);
            return openDotFloatExpSign();
        } else {
            throwException(LexErrorMessages.LITERAL_FLOAT_EXP_INVALID);
        }

        return null;
    }

    private Token openDotFloatExpSign() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return closeFloat();
        } else {
            throwException(LexErrorMessages.LITERAL_FLOAT_EXP_SIGN_INVALID);
        }

        return null;
    }

    private Token closeFloat() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return closeFloat();
        } else {
            checkFloat();
            return new Token(TokenType.floatLiteral, lexeme, line, column);
        }
    }

    private Token closeFloatExp() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return closeFloatExp();
        } else if (ch == 'e' || ch == 'E') {
            appendCharLexeme(ch);
            return openFloatExp();
        } else {
            checkFloat();
            return new Token(TokenType.floatLiteral, lexeme, line, column);
        }
    }

    private Token openFloatExp() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return closeFloat();
        } else if (ch == '+' || ch == '-') {
            appendCharLexeme(ch);
            return openFloatExpSign();
        } else {
            throwException(LexErrorMessages.LITERAL_FLOAT_EXP_INVALID);
        }

        return null;
    }

    private Token openFloatExpSign() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return closeFloat();
        } else {
            throwException(LexErrorMessages.LITERAL_FLOAT_EXP_SIGN_INVALID);
        }

        return null;
    }

    private Token openFloatDot() throws LexicalException {
        ch = readChar();

        if (Character.isDigit(ch)) {
            appendCharLexeme(ch);
            return closeFloatExp();
        } else if (ch == 'e' || ch == 'E') {
            appendCharLexeme(ch);
            return openFloatExp();
        } else {
            checkFloat();
            return new Token(TokenType.floatLiteral, lexeme, line, column);
        }
    }

    private void checkFloat() throws LexicalException {
        float f = Float.parseFloat(lexeme + "f");
        if (Float.isInfinite(f))
            throwException(LexErrorMessages.LITERAL_FLOAT_TOO_LARGE);
    }

    private void checkInteger() throws LexicalException {
        if (lexeme.length() > LexerConfig.MAX_INT_LENGTH)
            throwException(LexErrorMessages.LITERAL_INT_TOO_LARGE);

        // issue: Integer.MINVALUE value is false positive
        try {
            Integer.parseInt(lexeme);
        } catch (NumberFormatException e) {
            throwException(LexErrorMessages.LITERAL_INT_TOO_LARGE);
        }
    }

    private void saveLineIfError(int line) {
        if (errors.containsKey(line)) saveLine(line);
    }

    private void saveLine(int line) {
        String lineText = sourceManager.getLineText();
        if (!errors.containsKey(line)) {
            errors.put(line, new Pair<>(new ArrayList<>(), lineText));
        } else {
            errors.get(line).setSecond(lineText);
        }
    }

    private void saveError(String message) {
        if (!errors.containsKey(line))
            errors.put(line, new Pair<>(new ArrayList<>(), ""));

        errors.get(line).getFirst().add(
            new Error(
                message,
                lexeme,
                line,
                column,
                ErrorType.Lexical
            )
        );
    }

    private void throwException(String message) throws LexicalException {
        saveError(message);
        throw new LexicalException(message);
    }

    private void restart() {
        started = false;
    }

    private void appendCharLexeme(char ch) {
        if (ch != SourceManager.NEWLINE && ch != SourceManager.END_OF_FILE)
            lexeme += Character.toString(ch);
    }
}