package ru.mirea.insurance.ui.console;

public record Field(String key, String value) {

    public static Field of(String key, String value) {
        return new Field(key, value);
    }
}
