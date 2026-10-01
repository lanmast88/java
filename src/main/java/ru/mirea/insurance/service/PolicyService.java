package ru.mirea.insurance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import ru.mirea.insurance.exception.BusinessRuleException;
import ru.mirea.insurance.exception.EntityNotFoundException;
import ru.mirea.insurance.model.Client;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.model.PolicyStatus;
import ru.mirea.insurance.model.PolicyType;
import ru.mirea.insurance.repository.CrudRepository;
import ru.mirea.insurance.repository.PolicyRepository;

/**
 * Полис — основная сущность системы, поэтому здесь живут правила 1–3,
 * расчёт премии и переходы статусов. Зависимость от клиентов объявлена
 * интерфейсом CrudRepository<Client>: сервису достаточно контракта.
 */
@Service
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final CrudRepository<Client> clientRepository;

    public PolicyService(PolicyRepository policyRepository, CrudRepository<Client> clientRepository) {
        this.policyRepository = policyRepository;
        this.clientRepository = clientRepository;
    }

    /**
     * Оформление полиса: три бизнес-правила и премия, посчитанная по тарифу типа.
     * Премию пользователь не вводит — иначе её можно было бы занизить руками.
     */
    public Policy issue(int clientId, PolicyType type, BigDecimal insuredSum,
                        LocalDate startDate, LocalDate endDate) {
        // Правило 1: полис только на существующего клиента.
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Полис не оформлен: клиента с id=" + clientId + " нет в базе"));

        // Правило 2: положительная страховая сумма и корректный период.
        requireSumAndPeriod(insuredSum, startDate, endDate);

        // Правило 3: не больше одного действующего полиса одного типа на клиента.
        requireNoActivePolicyOfSameType(client, type, 0);

        BigDecimal premium = type.calculatePremium(insuredSum);
        Policy policy = new Policy(nextNumber(type, startDate), clientId, type,
                PolicyStatus.ACTIVE, insuredSum, premium, startDate, endDate);
        int id = policyRepository.save(policy);
        return findById(id);
    }

    public List<Policy> findAll() {
        return policyRepository.findAll();
    }

    public Policy findById(int id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Полис с id=" + id + " не найден"));
    }

    /** Второй способ поиска: по номеру полиса. */
    public Policy findByNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new BusinessRuleException("Номер полиса не может быть пустым");
        }
        return policyRepository.findByNumber(number.trim())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Полис с номером " + number.trim() + " не найден"));
    }

    public List<Policy> findByClientId(int clientId) {
        return policyRepository.findByClientId(clientId);
    }

    /** Фильтр 1: по виду страхования. */
    public List<Policy> filterByType(PolicyType type) {
        return policyRepository.findAll().stream()
                .filter(policy -> policy.getType() == type)
                .toList();
    }

    /** Фильтр 2: по статусу полиса. */
    public List<Policy> filterByStatus(PolicyStatus status) {
        return policyRepository.findAll().stream()
                .filter(policy -> policy.getStatus() == status)
                .toList();
    }

    /** Сортировка 1: по дате окончания — что заканчивается раньше, то и сверху. */
    public List<Policy> sortedByEndDate() {
        return policyRepository.findAll().stream()
                .sorted(Comparator.comparing(Policy::getEndDate))
                .toList();
    }

    /** Сортировка 2: по страховой сумме, от крупных договоров к мелким. */
    public List<Policy> sortedByInsuredSum() {
        return policyRepository.findAll().stream()
                .sorted(Comparator.comparing(Policy::getInsuredSum).reversed())
                .toList();
    }

    /**
     * Изменение условий: сумма и период проверяются теми же правилами, премия
     * пересчитывается заново. Закрытый договор менять нельзя.
     */
    public Policy change(int id, BigDecimal insuredSum, LocalDate startDate, LocalDate endDate) {
        Policy policy = findById(id);
        if (policy.getStatus() == PolicyStatus.CANCELLED || policy.getStatus() == PolicyStatus.EXPIRED) {
            throw new BusinessRuleException("Полис " + policy.getNumber() + " уже закрыт ("
                    + policy.getStatus().getTitle() + "), менять его условия нельзя");
        }
        requireSumAndPeriod(insuredSum, startDate, endDate);
        Policy changed = new Policy(policy.getId(), policy.getNumber(), policy.getClientId(),
                policy.getType(), policy.getStatus(), insuredSum,
                policy.getType().calculatePremium(insuredSum), startDate, endDate);
        policyRepository.update(changed);
        return findById(id);
    }

    /** Переход DRAFT → ACTIVE: черновик вступает в силу, правило 3 проверяется ещё раз. */
    public Policy activate(int id) {
        Policy policy = findById(id);
        requireStatus(policy, PolicyStatus.ACTIVE, PolicyStatus.DRAFT);
        Client client = clientRepository.findById(policy.getClientId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Клиента с id=" + policy.getClientId() + " нет в базе"));
        requireNoActivePolicyOfSameType(client, policy.getType(), policy.getId());
        return changeStatus(policy, PolicyStatus.ACTIVE);
    }

    /** Переход ACTIVE → CANCELLED: расторжение договора. Обратного пути нет. */
    public Policy cancel(int id) {
        Policy policy = findById(id);
        requireStatus(policy, PolicyStatus.CANCELLED, PolicyStatus.DRAFT, PolicyStatus.ACTIVE);
        return changeStatus(policy, PolicyStatus.CANCELLED);
    }

    /** Переход ACTIVE → EXPIRED: срок действия вышел. */
    public Policy expire(int id) {
        Policy policy = findById(id);
        requireStatus(policy, PolicyStatus.EXPIRED, PolicyStatus.ACTIVE);
        return changeStatus(policy, PolicyStatus.EXPIRED);
    }

    public void delete(int id) {
        Policy policy = findById(id);
        policyRepository.delete(policy.getId());
    }

    /** Строки для выгрузки в Excel: сервис собирает данные, util/ExcelExporter их пишет. */
    public List<PolicyReportRow> report() {
        List<Client> clients = clientRepository.findAll();
        return policyRepository.findAll().stream()
                .map(policy -> new PolicyReportRow(policy, clients.stream()
                        .filter(client -> client.getId() == policy.getClientId())
                        .findFirst()
                        .map(Client::getFullName)
                        .orElse("—")))
                .toList();
    }

    private void requireSumAndPeriod(BigDecimal insuredSum, LocalDate startDate, LocalDate endDate) {
        if (insuredSum == null || insuredSum.signum() <= 0) {
            throw new BusinessRuleException("Страховая сумма должна быть больше нуля");
        }
        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) {
            throw new BusinessRuleException("Дата начала должна быть раньше даты окончания");
        }
    }

    /** exceptId — полис, который сам же переводится в ACTIVE, он себе не конкурент. */
    private void requireNoActivePolicyOfSameType(Client client, PolicyType type, int exceptId) {
        policyRepository.findByClientId(client.getId()).stream()
                .filter(policy -> policy.getId() != exceptId)
                .filter(policy -> policy.getStatus() == PolicyStatus.ACTIVE)
                .filter(policy -> policy.getType() == type)
                .findFirst()
                .ifPresent(existing -> {
                    throw new BusinessRuleException("У клиента " + client.getFullName()
                            + " уже есть действующий полис " + type.getTitle()
                            + " — " + existing.getNumber());
                });
    }

    /** Запрещённый переход статуса отклоняется с объяснением, откуда он возможен. */
    private void requireStatus(Policy policy, PolicyStatus target, PolicyStatus... allowedFrom) {
        for (PolicyStatus status : allowedFrom) {
            if (policy.getStatus() == status) {
                return;
            }
        }
        StringBuilder allowed = new StringBuilder();
        for (int i = 0; i < allowedFrom.length; i++) {
            allowed.append(i > 0 ? " или " : "").append(allowedFrom[i].getTitle());
        }
        throw new BusinessRuleException("Полис " + policy.getNumber() + ": переход «"
                + policy.getStatus().getTitle() + "» → «" + target.getTitle()
                + "» запрещён, так можно только из «" + allowed + "»");
    }

    private Policy changeStatus(Policy policy, PolicyStatus status) {
        policy.setStatus(status);
        policyRepository.update(policy);
        return findById(policy.getId());
    }

    /** Номер вида KASKO-2026-0007: занятый номер пропускаем, в базе он UNIQUE. */
    private String nextNumber(PolicyType type, LocalDate startDate) {
        int sequence = policyRepository.findAll().size() + 1;
        while (true) {
            String number = String.format("%s-%d-%04d", type.name(), startDate.getYear(), sequence);
            if (policyRepository.findByNumber(number).isEmpty()) {
                return number;
            }
            sequence++;
        }
    }
}
