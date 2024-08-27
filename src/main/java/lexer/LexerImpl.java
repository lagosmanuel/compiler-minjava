package main.java.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import main.java.config.LexerConfig;
import main.java.model.Error;
import main.java.model.Pair;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.utils.Formater;
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
        this.errors = errors;
        lexeme = "";
        started = false;
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
            token = new Token(TokenType.dot, lexeme, line, column);
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
            return closeTimesOp();
        } else if (ch == '/') {
            return openComment();
        } else if (ch == '%') {
            return closeModOp();
        } else if (ch == '&') {
            return closeAndOp();
        } else if (ch == '|') {
            return closeOrOp();
        } else if (ch == '!') {
            return closeNotOp();
        } else if (ch == SourceManager.NEWLINE) {
            saveLineIfError(line);
            ch = readChar();
            return start();
        } else if (ch == SourceManager.END_OF_FILE) {
            saveLineIfError(line);
            token = new Token(TokenType.EOF, TokenMessages.EOF, line, column);
        }  else if (Character.isWhitespace(ch)) {
            ch = readChar();
            return start();
        } else {
            restart();
            throwException(LexErrorMessages.UNEXPECTED_SYMBOL);
        }

        restart();
        return token;
    }

    private Token idMetVar() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
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
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();

        if (Character.isLetterOrDigit(ch) || ch == '_') {
            appendCharLexeme(ch);
            return idClassVar();
        } else {
            return new Token(TokenType.idClassVar, lexeme, line, column);
        }
    }

    private Token digit() throws LexicalException {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();

        if (Character.isDigit(ch)) {
            if (lexeme.length() == LexerConfig.MAX_INT_LENGTH) {
                throwException(LexErrorMessages.LITERAL_INT_TOO_LONG);
            }
            appendCharLexeme(ch);
            return digit();
        } else {
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

        if (ch == SourceManager.END_OF_FILE) {
            throwException(LexErrorMessages.LITERAL_CHAR_NOT_CLOSED);
        }

        appendCharLexeme(ch);
        return closeCharLiteral();
    }

    private Token closeCharLiteral() throws LexicalException {
        ch = readChar();
        Token token = null;

        if (ch == '\'') {
            restart();
            appendCharLexeme(ch);
            token = new Token(TokenType.charLiteral, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            throwException(LexErrorMessages.LITERAL_CHAR_NOT_CLOSED);
        }

        return token;
    }

    private Token closeStringLiteral() throws LexicalException {
        ch = readChar();
        Token token = null;

        if (ch == '"') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.stringLiteral, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
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

        return token;
    }

    private Token escapeStringLiteral() throws LexicalException {
        ch = readChar();

        // TODO
        if (ch == SourceManager.NEWLINE) {
            throwException(LexErrorMessages.LITERAL_STR_BAD_ESCAPED);
        } else if (ch == SourceManager.END_OF_FILE) {
            throwException(LexErrorMessages.LITERAL_STR_BAD_ESCAPED);
        } else if (Character.isWhitespace(ch)) {
            throwException(LexErrorMessages.LITERAL_STR_BAD_ESCAPED);
        }

        appendCharLexeme(ch);
        return closeStringLiteral();
    }

    private Token closeAndOp() throws LexicalException {
        ch = readChar();
        Token token = null;

        if (ch == '&') {
            restart();
            appendCharLexeme(ch);
            token = new Token(TokenType.opAnd, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else if (ch == '=') {
            restart();
            appendCharLexeme(ch);
            token = new Token(TokenType.opAndAssign, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        }
        else {
            throwException(LexErrorMessages.OP_AND_INVALID);
        }

        return token;
    }

    private Token closeOrOp() throws LexicalException {
        ch = readChar();
        Token token = null;

        if (ch == '|') {
            restart();
            appendCharLexeme(ch);
            token = new Token(TokenType.opOr, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else if (ch == '=') {
            restart();
            appendCharLexeme(ch);
            token = new Token(TokenType.opOrAssign, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            throwException(LexErrorMessages.OP_OR_INVALID);
        }

        return token;
    }

    private Token openComment() throws LexicalException {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '/') {
            appendCharLexeme(ch);
            return lineComment();
        } else if (ch == '*') {
            appendCharLexeme(ch);
            return blockComment();
        } else if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opDivAssign, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opDiv, lexeme, line, column);
        }

        return token;
    }

    private Token closeGreaterOp() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opGreaterEqual, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opGreater, lexeme, line, column);
        }

        return token;
    }

    private Token closeLessOp() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opLessEqual, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opLess, lexeme, line, column);
        }

        return token;
    }

    private Token closeEqualOp() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opEqual, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opAssign, lexeme, line, column);
        }

        return token;
    }

    private Token closePlusOp() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opPlusAssign, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opPlus, lexeme, line, column);
        }

        return token;
    }

    private Token closeMinusOp() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opMinusAssign, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opMinus, lexeme, line, column);
        }

        return token;
    }

    private Token closeTimesOp() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opTimesAssign, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opTimes, lexeme, line, column);
        }

        return token;
    }

    private Token lineComment() throws LexicalException {
        do {
            ch = readChar();
            // appendCharLexeme(ch); TODO
        } while (ch != SourceManager.NEWLINE && ch != SourceManager.END_OF_FILE);

        return start();
    }

    private Token closeModOp() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opModAssign, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opMod, lexeme, line, column);
        }

        return token;
    }

    private Token closeNotOp() {
        int line = sourceManager.getLineNumber();
        int column = sourceManager.getColumnNumber();
        ch = readChar();
        Token token;

        if (ch == '=') {
            appendCharLexeme(ch);
            restart();
            token = new Token(TokenType.opNotEqual, lexeme,
                    sourceManager.getLineNumber(), sourceManager.getColumnNumber());
        } else {
            token = new Token(TokenType.opNot, lexeme, line, column);
        }

        return token;
    }

    private Token blockComment() throws LexicalException {
        boolean hasSeenAsterisk = false;

        do {
            ch = readChar();
            if (ch != SourceManager.END_OF_FILE)
                appendCharLexeme(ch);

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

    private void saveLineIfError(int line) {
        if (errors.containsKey(line)) saveLine(line);
    }

    private String saveLine(int line) {
        String lineText = sourceManager.getLineText();
        if (!errors.containsKey(line)) {
            errors.put(line, new Pair<>(new ArrayList<>(), lineText));
        } else {
            errors.get(line).setSecond(lineText);
        }
        return lineText;
    }

    private Error saveError(String message) {
        if (!errors.containsKey(line))
            errors.put(line, new Pair<>(new ArrayList<>(), ""));

        Error error = new Error(message, lexeme, line, column);
        errors.get(line).getFirst().add(error);
        return error;
    }

    private void throwException(String message) throws LexicalException {
        Error error = saveError(message);
        String lineText = saveLine(sourceManager.getLineNumber());
        throw new LexicalException(Formater.formatError(error, lineText));
    }

    private void restart() {
        started = false;
    }

    private void appendCharLexeme(char ch) {
        if (ch != SourceManager.NEWLINE && ch != SourceManager.END_OF_FILE) // TODO
            lexeme += Character.toString(ch);
    }
}
