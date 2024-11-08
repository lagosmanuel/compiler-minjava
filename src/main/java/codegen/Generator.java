package main.java.codegen;

import java.io.FileWriter;
import java.io.IOException;

public class Generator {
    final FileWriter file_writer;

    public Generator(String file_name) {
        try {
            file_writer = new FileWriter(file_name);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void write(String... args) {
        try {
            for (String arg:args)
                file_writer.write(arg + " ");
            file_writer.write("\n");
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void close() {
        try {
            file_writer.close();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
