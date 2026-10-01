package ru.mirea.insurance.model;

/** Жизненный цикл полиса: DRAFT → ACTIVE → EXPIRED / CANCELLED. Обратных переходов нет. */
public enum PolicyStatus {
    DRAFT("черновик"),
    ACTIVE("действует"),
    EXPIRED("истёк"),
    CANCELLED("расторгнут");

    private final String title;

    PolicyStatus(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
}
