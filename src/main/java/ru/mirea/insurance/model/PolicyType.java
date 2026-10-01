package ru.mirea.insurance.model;

import java.math.BigDecimal;

public enum PolicyType {
    OSAGO("ОСАГО", "0.05"),
    KASKO("КАСКО", "0.08"),
    DMS("ДМС", "0.03"),
    PROPERTY("Имущество", "0.02");

    private final String title;
    private final BigDecimal baseRate;

    PolicyType(String title, String baseRate) {
        this.title = title;
        this.baseRate = new BigDecimal(baseRate);
    }

    public String getTitle() { return title; }

    public BigDecimal calculatePremium(BigDecimal insuredSum) {
        return insuredSum.multiply(baseRate);
    }
}