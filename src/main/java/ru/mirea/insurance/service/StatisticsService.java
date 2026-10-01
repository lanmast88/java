package ru.mirea.insurance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ru.mirea.insurance.model.Claim;
import ru.mirea.insurance.model.ClaimStatus;
import ru.mirea.insurance.model.Client;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.model.PolicyStatus;
import ru.mirea.insurance.model.PolicyType;
import ru.mirea.insurance.repository.ClaimRepository;
import ru.mirea.insurance.repository.ClientRepository;
import ru.mirea.insurance.repository.PolicyRepository;

/**
 * Статистика на Stream API. Поток не хранит данные и не меняет коллекцию:
 * filter/map ленивы и выполняются только при вызове count/reduce/collect.
 */
@Service
public class StatisticsService {
    private final ClientRepository clientRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public StatisticsService(ClientRepository clientRepository, PolicyRepository policyRepository,
                             ClaimRepository claimRepository) {
        this.clientRepository = clientRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public Statistics collect() {
        List<Client> clients = clientRepository.findAll();
        List<Policy> policies = policyRepository.findAll();
        List<Claim> claims = claimRepository.findAll();

        long active = policies.stream()
                .filter(policy -> policy.getStatus() == PolicyStatus.ACTIVE)
                .count();

        BigDecimal totalPremium = policies.stream()
                .map(Policy::getPremium)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Claim> paid = claims.stream()
                .filter(claim -> claim.getStatus() == ClaimStatus.PAID)
                .toList();

        BigDecimal totalPayout = paid.stream()
                .map(Claim::getPayout)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Деньги делим BigDecimal-ом с явным округлением, а не double.
        BigDecimal averagePayout = paid.isEmpty() ? BigDecimal.ZERO
                : totalPayout.divide(BigDecimal.valueOf(paid.size()), 2, RoundingMode.HALF_UP);

        Map<PolicyType, Long> byType = policies.stream()
                .collect(Collectors.groupingBy(Policy::getType, Collectors.counting()));

        return new Statistics(clients.size(), policies.size(), active,
                totalPremium, totalPayout, averagePayout, byType);
    }
}
