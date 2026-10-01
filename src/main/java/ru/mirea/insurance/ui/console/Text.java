package ru.mirea.insurance.ui.console;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Text {
    public static final String ELLIPSIS = "…";

    private static final Pattern ANSI = Pattern.compile("\u001B\\[[0-9;]*m");

    private Text() {
    }

    public static String stripAnsi(String s) {
        return ANSI.matcher(s).replaceAll("");
    }

    public static boolean hasAnsi(String s) {
        return ANSI.matcher(s).find();
    }

    public static int visibleLength(String s) {
        return stripAnsi(s).length();
    }

    public static String padRight(String s, int width) {
        return s + " ".repeat(Math.max(0, width - visibleLength(s)));
    }

    public static String padLeft(String s, int width) {
        return " ".repeat(Math.max(0, width - visibleLength(s))) + s;
    }

    public static String truncate(String s, int width) {
        if (s.length() <= width) {
            return s;
        }
        return s.substring(0, width - ELLIPSIS.length()) + ELLIPSIS;
    }

    public static List<String> wrap(String s, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : s.split(" ")) {
            if (!current.isEmpty() && current.length() + 1 + word.length() > width) {
                lines.add(current.toString());
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append(' ');
            }
            current.append(word);
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }
}
