package ru.mirea.insurance.ui.console;

public class InputClosedException extends RuntimeException {

    public InputClosedException() {
        super("Ввод закрыт");
    }
}
