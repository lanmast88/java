package ru.mirea.insurance.ui.console;

public enum Tone {
    BOLD("1"),
    DIM("2"),
    RED("31"),
    GREEN("32"),
    YELLOW("33"),
    CYAN("36");

    private static final String ESC = "\u001B[";
    static final String RESET = ESC + "0m";

    private final String code;

    Tone(String code) {
        this.code = code;
    }

    public String paint(String text) {
        return paint(text, this);
    }

    public static String paint(String text, Tone... tones) {
        if (text.isEmpty() || tones.length == 0) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        for (Tone tone : tones) {
            result.append(ESC).append(tone.code).append('m');
        }
        return result.append(text).append(RESET).toString();
    }
}
