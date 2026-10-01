package ru.mirea.insurance.ui.console;

public record MenuItem(int number, String label) {

    public static MenuItem of(int number, String label) {
        return new MenuItem(number, label);
    }
}
