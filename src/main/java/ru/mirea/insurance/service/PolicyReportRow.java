package ru.mirea.insurance.service;

import ru.mirea.insurance.model.Policy;

/**
 * Полис вместе с ФИО страхователя — одна строка отчёта. Нужен, чтобы экспорт
 * не ходил в репозитории сам и не знал, откуда берутся имена клиентов.
 */
public record PolicyReportRow(Policy policy, String clientName) {
}
