package ru.mirea.insurance.model;

/** Жизненный цикл убытка: SUBMITTED → APPROVED / REJECTED → PAID. */
public enum ClaimStatus {
    SUBMITTED("заявлен"),
    APPROVED("одобрен"),
    REJECTED("отклонён"),
    PAID("выплачен");

    private final String title;

    ClaimStatus(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
}
