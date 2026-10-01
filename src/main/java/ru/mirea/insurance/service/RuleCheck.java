package ru.mirea.insurance.service;

/**
 * Результат одной намеренно неверной операции: что проверяли, что вызвали и
 * каким сообщением система это отклонила.
 *
 * @param number   номер бизнес-правила
 * @param rule     формулировка правила
 * @param call     вызов, который его нарушает
 * @param outcome  сообщение системы
 * @param rejected отклонил ли сервис вызов (если нет — правило не работает)
 */
public record RuleCheck(int number, String rule, String call, String outcome, boolean rejected) {
}
