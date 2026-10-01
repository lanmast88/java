package ru.mirea.insurance.ui.console;

public record Column(String header, int width, boolean alignRight) {

    public static Column left(String header, int width) {
        return new Column(header, width, false);
    }

    public static Column right(String header, int width) {
        return new Column(header, width, true);
    }
}
