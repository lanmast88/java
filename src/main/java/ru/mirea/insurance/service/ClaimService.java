package ru.mirea.insurance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import ru.mirea.insurance.exception.BusinessRuleException;
import ru.mirea.insurance.exception.EntityNotFoundException;
import ru.mirea.insurance.model.Claim;
import ru.mirea.insurance.model.ClaimStatus;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.model.PolicyStatus;
import ru.mirea.insurance.repository.ClaimRepository;
import ru.mirea.insurance.repository.PolicyRepository;
import ru.mirea.insurance.util.Formats;

/**
 * Страховые случаи: правила 4 и 5 плюс переходы статусов убытка
 * SUBMITTED → APPROVED / REJECTED → PAID.
 */
@Service
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;

    public ClaimService(ClaimRepository claimRepository, PolicyRepository policyRepository) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
    }

    /**
     * Правило 4: убыток регистрируется только по действующему полису и только
     * если дата события попадает в период действия договора.
     */
    public Claim submit(int policyId, LocalDate eventDate, String description,
                        BigDecimal claimedAmount) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Убыток не заявлен: полиса с id=" + policyId + " нет в базе"));

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new BusinessRuleException("Полис " + policy.getNumber() + " не действует ("
                    + policy.getStatus().getTitle() + "), убыток по нему заявить нельзя");
        }
        if (eventDate == null || eventDate.isBefore(policy.getStartDate())
                || eventDate.isAfter(policy.getEndDate())) {
            throw new BusinessRuleException("Дата события "
                    + (eventDate == null ? "не указана" : Formats.date(eventDate))
                    + " вне периода действия полиса " + policy.getNumber() + " ("
                    + Formats.period(policy.getStartDate(), policy.getEndDate()) + ")");
        }
        LocalDate today = LocalDate.now();
        if (eventDate.isAfter(today)) {
            throw new BusinessRuleException("Дата события " + Formats.date(eventDate)
                    + " ещё не наступила — убыток нельзя заявить задним числом из будущего");
        }
        if (claimedAmount == null || claimedAmount.signum() <= 0) {
            throw new BusinessRuleException("Заявленная сумма убытка должна быть больше нуля");
        }
        if (description == null || description.isBlank()) {
            throw new BusinessRuleException("Описание страхового случая обязательно");
        }

        Claim claim = new Claim(policyId, eventDate, today, description.trim(), claimedAmount);
        int id = claimRepository.save(claim);
        return findById(id);
    }

    public List<Claim> findAll() {
        return claimRepository.findAll();
    }

    public Claim findById(int id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Убыток с id=" + id + " не найден"));
    }

    public List<Claim> findByPolicyId(int policyId) {
        if (policyRepository.findById(policyId).isEmpty()) {
            throw new EntityNotFoundException("Полис с id=" + policyId + " не найден");
        }
        return claimRepository.findByPolicyId(policyId);
    }

    /** Переход SUBMITTED → APPROVED. */
    public Claim approve(int id) {
        Claim claim = findById(id);
        requireStatus(claim, ClaimStatus.APPROVED, ClaimStatus.SUBMITTED);
        claim.setStatus(ClaimStatus.APPROVED);
        claimRepository.update(claim);
        return findById(id);
    }

    /** Переход SUBMITTED → REJECTED. */
    public Claim reject(int id) {
        Claim claim = findById(id);
        requireStatus(claim, ClaimStatus.REJECTED, ClaimStatus.SUBMITTED);
        claim.setStatus(ClaimStatus.REJECTED);
        claimRepository.update(claim);
        return findById(id);
    }

    /**
     * Правило 5: выплата возможна только по одобренному убытку (REJECTED → PAID
     * запрещён) и не больше страховой суммы за вычетом франшизы.
     */
    public Claim pay(int id, BigDecimal payout) {
        Claim claim = requirePayable(id);

        Policy policy = policyRepository.findById(claim.getPolicyId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Полис с id=" + claim.getPolicyId() + " не найден"));

        if (payout == null || payout.signum() <= 0) {
            throw new BusinessRuleException("Сумма выплаты должна быть больше нуля");
        }
        BigDecimal limit = policy.getType().maxPayout(policy.getInsuredSum());
        if (payout.compareTo(limit) > 0) {
            throw new BusinessRuleException("Выплата " + Formats.rub(payout)
                    + " превышает лимит по полису " + policy.getNumber() + ": страховая сумма "
                    + Formats.rub(policy.getInsuredSum()) + " минус франшиза "
                    + Formats.rub(policy.getType().calculateDeductible(policy.getInsuredSum()))
                    + " = " + Formats.rub(limit));
        }

        claim.setPayout(payout);
        claim.setStatus(ClaimStatus.PAID);
        claimRepository.update(claim);
        return findById(id);
    }

    /**
     * Можно ли вообще платить по этому убытку. Отдельный метод нужен, чтобы меню
     * спросило сумму только после проверки статуса, а само правило осталось в сервисе.
     */
    public Claim requirePayable(int id) {
        Claim claim = findById(id);
        requireStatus(claim, ClaimStatus.PAID, ClaimStatus.APPROVED);
        return claim;
    }

    private void requireStatus(Claim claim, ClaimStatus target, ClaimStatus allowedFrom) {
        if (claim.getStatus() != allowedFrom) {
            throw new BusinessRuleException("Убыток #" + claim.getId() + ": переход «"
                    + claim.getStatus().getTitle() + "» → «" + target.getTitle()
                    + "» запрещён, так можно только из «" + allowedFrom.getTitle() + "»");
        }
    }
}
