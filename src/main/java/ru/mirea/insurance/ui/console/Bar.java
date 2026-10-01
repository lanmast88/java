package ru.mirea.insurance.ui.console;

public record Bar(String label, long value) {

    public static Bar of(String label, long value) {
        return new Bar(label, value);
    }
}
