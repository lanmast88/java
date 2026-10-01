package ru.mirea.insurance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ru.mirea.insurance.exception.BusinessRuleException;
import ru.mirea.insurance.exception.EntityNotFoundException;
import ru.mirea.insurance.model.Claim;
import ru.mirea.insurance.model.ClaimStatus;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.model.PolicyStatus;
import ru.mirea.insurance.model.PolicyType;
import ru.mirea.insurance.util.Formats;

/**
 * Сценарий демонстрации: пять заведомо неверных вызовов подряд. Ни один из них
 * ничего не записывает в базу — каждый отклоняется бизнес-правилом, а программа
 * продолжает работать. Данные для вызовов берутся из того, что есть в базе.
 */
@Service
public class RulesDemoService {
    private static final int MISSING_CLIENT_ID = 999_999;

    private final PolicyService policyService;
    private final ClaimService claimService;

    public RulesDemoService(PolicyService policyService, ClaimService claimService) {
        this.policyService = policyService;
        this.claimService = claimService;
    }

    public List<RuleCheck> runAll() {
        List<RuleCheck> checks = new ArrayList<>();
        Optional<Policy> active = policyService.filterByStatus(PolicyStatus.ACTIVE).stream().findFirst();
        Optional<Claim> approved = claimService.findAll().stream()
                .filter(claim -> claim.getStatus() == ClaimStatus.APPROVED)
                .findFirst();

        checks.add(check(1, "Полис нельзя оформить на несуществующего клиента",
                "issue(clientId=" + MISSING_CLIENT_ID + ", ОСАГО, " + Formats.rub(new BigDecimal("500000")) + ")",
                () -> policyService.issue(MISSING_CLIENT_ID, PolicyType.OSAGO,
                        new BigDecimal("500000"), LocalDate.now(), LocalDate.now().plusYears(1))));

        checks.add(active.map(policy -> check(2,
                "Период действия должен быть корректным, а страховая сумма — положительной",
                "issue(clientId=" + policy.getClientId() + ", КАСКО, " + Formats.rub(new BigDecimal("800000")) + ", период 01.01.2027 – 01.01.2026)",
                () -> policyService.issue(policy.getClientId(), PolicyType.KASKO,
                        new BigDecimal("800000"), LocalDate.of(2027, 1, 1), LocalDate.of(2026, 1, 1))))
                .orElseGet(() -> skipped(2, "Период действия должен быть корректным, "
                        + "а страховая сумма — положительной")));

        checks.add(active.map(policy -> check(3,
                "У клиента не может быть двух действующих полисов одного типа",
                "issue(clientId=" + policy.getClientId() + ", " + policy.getType().getTitle()
                        + ") при живом полисе " + policy.getNumber(),
                () -> policyService.issue(policy.getClientId(), policy.getType(),
                        policy.getInsuredSum(), LocalDate.now(), LocalDate.now().plusYears(1))))
                .orElseGet(() -> skipped(3, "У клиента не может быть двух действующих полисов одного типа")));

        checks.add(active.map(policy -> check(4,
                "Убыток заявляется только по действующему полису и только за дату внутри его периода",
                "submit(policyId=" + policy.getId() + ", дата события "
                        + Formats.date(policy.getStartDate().minusDays(10)) + ")",
                () -> claimService.submit(policy.getId(), policy.getStartDate().minusDays(10),
                        "Событие до начала действия полиса", new BigDecimal("50000"))))
                .orElseGet(() -> skipped(4, "Убыток заявляется только по действующему полису "
                        + "и только за дату внутри его периода")));

        checks.add(approved.map(claim -> {
            Policy policy = policyService.findById(claim.getPolicyId());
            BigDecimal tooMuch = policy.getInsuredSum().multiply(new BigDecimal("10"));
            return check(5, "Выплата не больше страховой суммы за вычетом франшизы "
                            + "и только по одобренному убытку",
                    "pay(claimId=" + claim.getId() + ", " + Formats.rub(tooMuch)
                            + " по полису на " + Formats.rub(policy.getInsuredSum()) + ")",
                    () -> claimService.pay(claim.getId(), tooMuch));
        }).orElseGet(() -> skipped(5, "Выплата не больше страховой суммы за вычетом франшизы "
                + "и только по одобренному убытку")));

        return checks;
    }

    /**
     * Вызов обязан упасть одним из наших исключений. Если он прошёл — правило
     * не работает, и это видно в отчёте.
     */
    private RuleCheck check(int number, String rule, String call, Runnable operation) {
        try {
            operation.run();
            return new RuleCheck(number, rule, call,
                    "вызов прошёл — правило не сработало, это ошибка", false);
        } catch (BusinessRuleException | EntityNotFoundException e) {
            return new RuleCheck(number, rule, call, e.getMessage(), true);
        }
    }

    private RuleCheck skipped(int number, String rule) {
        return new RuleCheck(number, rule, "—",
                "в базе нет подходящих данных: нужен действующий полис, а для правила 5 — "
                        + "одобренный убыток", false);
    }
}
