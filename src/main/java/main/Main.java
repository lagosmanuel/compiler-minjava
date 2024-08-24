package main.java.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.java.lexer.Lexer;
import main.java.lexer.LexerImpl;
import main.java.model.Error;
import main.java.model.Pair;
import main.java.model.Token;
import main.java.model.TokenType;
import main.java.utils.Formater;
import main.java.utils.sourcemanager.SourceManager;
import main.java.utils.sourcemanager.SourceManagerImpl;
import main.java.config.LexerConfig;
import main.java.messages.ErrorMessages;
import main.java.exeptions.LexicalException;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    private static SourceManager sourceManager;
    private static Lexer lexer;
    private static Map<Integer, Pair<List<Error>, String>> errors;

    public static void main(String[] args) {
        init();

        if (args.length == 1) {
            if (loadFile(args[0])) {
                showTokens();
                closeFile();
            }
        } else {
            System.out.println(ErrorMessages.BAD_USAGE);
        }
    }

    private static void init() {
        sourceManager = new SourceManagerImpl();
        errors = new HashMap<>();
        lexer = new LexerImpl(sourceManager, errors);
    }

    private static boolean loadFile(String filename) {
        boolean loaded = false;
        try {
            sourceManager.open(filename);
            loaded = true;
        } catch (FileNotFoundException error) {
            System.out.println(error.getMessage());
        }
        return loaded;
    }

    private static void closeFile() {
        try {
            sourceManager.close();
        } catch (IOException error) {
            System.out.println(error.getMessage());
        }
    }

    private static void showTokens() {
        Token token;
        boolean onRecovery = false;

        do {
            token = null;

            try {
                token = lexer.nextToken();
                if (token != null) System.out.println(token);
            } catch (LexicalException exception) {
                if (LexerConfig.CONTINUE_ON_ERROR) onRecovery = true;
                else System.out.print(exception.getMessage());
            }
        } while ((token != null && token.getType() != TokenType.EOF) || (token == null && onRecovery));

        if (onRecovery) {
            showErrors(errors);
        } else if (token != null && token.getType() == TokenType.EOF) {
            System.out.println(ErrorMessages.SUCCESS);
        }
    }

    private static void showErrors(Map<Integer, Pair<List<Error>, String>> errors) {
        System.out.println();
        for (Map.Entry<Integer, Pair<List<Error>, String>> entry : errors.entrySet()) {
            for (Error error : entry.getValue().getFirst()) {
                System.out.println(Formater.formatError(error, entry.getValue().getSecond()));
            }
        }
    }
}