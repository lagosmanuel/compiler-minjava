package main.java.utils.sourcemanager;

import main.java.config.LexerConfig;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface SourceManager {
    void open(String filePath) throws FileNotFoundException;
    void close() throws IOException;
    char getNextChar() throws IOException;
    int getLineNumber();
    int getColumnNumber();
    String getLineText();
    String getLineText(int lineNumber);
    char END_OF_FILE = LexerConfig.END_OF_FILE;
    char NEWLINE = LexerConfig.NEWLINE;
    char CARRY_RETURN = LexerConfig.CARRY_RETURN;
}
