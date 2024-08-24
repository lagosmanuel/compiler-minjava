package main.java.utils;

import main.java.messages.ErrorMessages;
import main.java.messages.LexErrorMessages;
import main.java.model.Error;

public class Formater {
    public static String formatError(Error error, String errorLine) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format(LexErrorMessages.FORMAT,
                error.getLine(),
                error.getColumn(),
                error.getLexeme(),
                error.getMessage()
        ));
        stringBuilder.append(errorLine);
        stringBuilder.append(formatErrorDetail(error.getColumn() - 1));
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
}
