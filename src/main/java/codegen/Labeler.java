package main.java.codegen;

import java.util.HashSet;
import main.java.config.CodegenConfig;

public class Labeler {
    private static final HashSet<String> labels = new HashSet<>();
    private static int label_counter = 0;

    public static void reset() {
        label_counter = 0;
        labels.clear();
    }

    private static int increment() {
        return label_counter++;
    }

    public static String getLabel(boolean indexed, String format, String... args) {
        String label = String.format(format, args) + (indexed? CodegenConfig.INDEX_SUFFIX.formatted(increment()):"");
        if (labels.contains(label))
            throw new RuntimeException(String.format(CodegenConfig.LABEL_DUPLICATED, label));
        labels.add(label);
        return label;
    }

    public static String getLabel(String format, String... args) {
        return getLabel(false, format, args);
    }
}
