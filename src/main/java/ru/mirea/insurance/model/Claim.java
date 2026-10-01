package ru.mirea.insurance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Claim {

    private final int id;
    private final int policyId;
    private final LocalDate eventDate;
    private final LocalDate submittedAt;
    private final String description;
    private final BigDecimal claimedAmount;
    private BigDecimal payout;
    private ClaimStatus status;

    public Claim(int id, int policyId, LocalDate eventDate, LocalDate submittedAt,
                 String description, BigDecimal claimedAmount, BigDecimal payout,
                 ClaimStatus status) {
        this.id = id;
        this.policyId = policyId;
        this.eventDate = eventDate;
        this.submittedAt = submittedAt;
        this.description = description;
        this.claimedAmount = claimedAmount;
        this.payout = payout;
        this.status = status;
    }

    public Claim(int policyId, LocalDate eventDate, LocalDate submittedAt,
                 String description, BigDecimal claimedAmount) {
        this(0, policyId, eventDate, submittedAt, description,
                claimedAmount, BigDecimal.ZERO, ClaimStatus.SUBMITTED);
    }

    public int getId() { return id; }
    public int getPolicyId() { return policyId; }
    public LocalDate getEventDate() { return eventDate; }
    public LocalDate getSubmittedAt() { return submittedAt; }
    public String getDescription() { return description; }
    public BigDecimal getClaimedAmount() { return claimedAmount; }
    public BigDecimal getPayout() { return payout; }
    public ClaimStatus getStatus() { return status; }

    public void setStatus(ClaimStatus status) { this.status = status; }
    public void setPayout(BigDecimal payout) { this.payout = payout; }

    @Override
    public String toString() {
        return String.format("#%d по полису #%d, событие: %s, заявлено: %.2f, выплачено: %.2f, статус: %s",
                id, policyId, eventDate, claimedAmount, payout, status);
    }
}