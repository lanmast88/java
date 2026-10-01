package ru.mirea.insurance.service;

import java.math.BigDecimal;
import java.util.Map;

import ru.mirea.insurance.model.PolicyType;

/**
 * Показатели по портфелю договоров. Отдельная запись нужна, чтобы сервис считал,
 * а меню только печатало — считать в UI нельзя.
 */
public record Statistics(long clients, long policies, long activePolicies,
                         BigDecimal totalPremium, BigDecimal totalPayout,
                         BigDecimal averagePayout, Map<PolicyType, Long> policiesByType) {
}
