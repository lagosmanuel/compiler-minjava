package main.java.utils;

import main.java.messages.ErrorMessages;
import main.java.messages.ParserErrorMessages;
import main.java.model.Error;
import main.java.model.Token;

import java.util.List;

public class Formater {
    public static String formatError(Error error, String errorLine) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(ErrorMessages.FORMAT,
                error.getType().toString(),
                error.getLine(),
                error.getColumn(),
                error.getLexeme(),
                error.getMessage()
        ));
        if (!errorLine.isEmpty()) {
            stringBuilder.append(errorLine);
            stringBuilder.append(formatErrorDetail(error.getColumn() - 1));
        }
        stringBuilder.append(String.format(ErrorMessages.CODE_FORMAT,
                error.getLexeme(),
                error.getLine())
        );
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private static String formatErrorDetail(int offset) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        stringBuilder.append(" ".repeat(offset));
        stringBuilder.append(ErrorMessages.POINTER);
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static String expectedResult(List<String> expectedList, Token token) {
        StringBuilder expectedString = new StringBuilder();
        for (int i = 0; i < expectedList.size(); ++i) {
            expectedString.append(expectedList.get(i));
            if (i < expectedList.size() - 1) expectedString.append(" or ");
        }
        return String.format(
            ParserErrorMessages.EXPECTED_RESULT,
            expectedString,
            token.getType()
        );
    }
}
