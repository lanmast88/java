package ru.mirea.insurance.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Вид страхования. Enum с полями: рядом с типом лежат его базовый тариф и
 * доля безусловной франшизы, поэтому новый вид страхования не требует правок
 * в сервисах — ни одного if по типу полиса в коде нет.
 */
public enum PolicyType {
    OSAGO("ОСАГО", "0.05", "0.00"),
    KASKO("КАСКО", "0.08", "0.01"),
    DMS("ДМС", "0.03", "0.00"),
    PROPERTY("Имущество", "0.02", "0.02");

    private final String title;
    private final BigDecimal baseRate;
    private final BigDecimal deductibleRate;

    PolicyType(String title, String baseRate, String deductibleRate) {
        this.title = title;
        this.baseRate = new BigDecimal(baseRate);
        this.deductibleRate = new BigDecimal(deductibleRate);
    }

    public String getTitle() { return title; }

    public BigDecimal getBaseRate() { return baseRate; }

    /** Премия = страховая сумма × тариф. Считается здесь, руками не вводится. */
    public BigDecimal calculatePremium(BigDecimal insuredSum) {
        return insuredSum.multiply(baseRate).setScale(2, RoundingMode.HALF_UP);
    }

    /** Безусловная франшиза — часть убытка, которую страховщик не возмещает. */
    public BigDecimal calculateDeductible(BigDecimal insuredSum) {
        return insuredSum.multiply(deductibleRate).setScale(2, RoundingMode.HALF_UP);
    }

    /** Предел выплаты по правилу 5: страховая сумма за вычетом франшизы. */
    public BigDecimal maxPayout(BigDecimal insuredSum) {
        return insuredSum.subtract(calculateDeductible(insuredSum));
    }
}
