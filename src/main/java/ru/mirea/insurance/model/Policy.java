package ru.mirea.insurance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Policy {

    private final int id;
    private final String number;
    private final int clientId;
    private final PolicyType type;
    private PolicyStatus status;
    private final BigDecimal insuredSum;
    private final BigDecimal premium;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public Policy(int id, String number, int clientId, PolicyType type,
                  PolicyStatus status, BigDecimal insuredSum, BigDecimal premium,
                  LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.number = number;
        this.clientId = clientId;
        this.type = type;
        this.status = status;
        this.insuredSum = insuredSum;
        this.premium = premium;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Policy(String number, int clientId, PolicyType type,
                  PolicyStatus status, BigDecimal insuredSum, BigDecimal premium,
                  LocalDate startDate, LocalDate endDate) {
        this(0, number, clientId, type, status, insuredSum, premium, startDate, endDate);
    }

    public int getId() { return id; }
    public String getNumber() { return number; }
    public int getClientId() { return clientId; }
    public PolicyType getType() { return type; }
    public PolicyStatus getStatus() { return status; }
    public BigDecimal getInsuredSum() { return insuredSum; }
    public BigDecimal getPremium() { return premium; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    public void setStatus(PolicyStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("#%d [%s] %s, сумма: %.2f, премия: %.2f, %s — %s",
                id, number, type.getTitle(), insuredSum, premium, startDate, endDate);
    }
}