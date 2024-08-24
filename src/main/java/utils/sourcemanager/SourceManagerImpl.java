package main.java.utils.sourcemanager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SourceManagerImpl implements SourceManager {
    private int line;
    private int column;
    private boolean hasLineEnded;
    private FileReader fileReader;
    private final StringBuilder stringBuilder;

    public SourceManagerImpl() {
        line = 1;
        column = 0;
        hasLineEnded = false;
        stringBuilder = new StringBuilder();
    }

    @Override
    public void open(String filePath) throws FileNotFoundException {
        fileReader = new FileReader(filePath);
    }

    @Override
    public void close() throws IOException {
        fileReader.close();
    }

    @Override
    public char getNextChar() throws IOException {
        int ch_code = fileReader.read();
        char ch = ch_code != -1? (char) ch_code:END_OF_FILE;

        if (hasLineEnded) {
            resetLine();
            hasLineEnded = false;
        }

        if (ch == NEWLINE) {
            hasLineEnded = true;
        } else if (ch == CARRY_RETURN) {
            if (fileReader.ready() && fileReader.read() != NEWLINE) {
                fileReader.skip(-1);
            }
            hasLineEnded = true;
            ch = NEWLINE;
        }
        ++column;

        if (ch != NEWLINE && ch != END_OF_FILE) stringBuilder.append(ch);
        return ch;
    }

    private void resetLine() {
        ++line;
        column = 0;
        stringBuilder.delete(0, stringBuilder.length());
    }

    @Override
    public int getLineNumber() {
        return line;
    }

    @Override
    public int getColumnNumber() {
        return column;
    }

    @Override
    public String getLineText() {
        return stringBuilder.toString();
    }
}
