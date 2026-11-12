package com.seguros.polizas.domain.model;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.domain.event.PolicyCancelledEvent;
import com.seguros.polizas.domain.event.PolicyRenewedEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root for the Policy domain.
 *
 * <p>Pure domain model: decoupled from JPA annotations or persistence logic.
 * Encapsulates core business invariants (premium calculation, IPC-based renewal,
 * cascade cancellation) and tracks domain events emitted during state mutations.</p>
 */
public class Policy {

    private Long id;
    private String policyNumber;
    private PolicyType type;
    private PolicyStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer coverageMonths;
    private BigDecimal monthlyRent;
    private BigDecimal totalPremium;
    private String policyHolder;
    private String insured;
    private String beneficiary;

    private final List<Risk> risks = new ArrayList<>();
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Policy() {
    }

    public Policy(String policyNumber, PolicyType type, PolicyStatus status,
                  LocalDate startDate, LocalDate endDate, Integer coverageMonths,
                  BigDecimal monthlyRent, String policyHolder, String insured, String beneficiary) {
        this.policyNumber = policyNumber;
        this.type = type;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.coverageMonths = coverageMonths;
        this.monthlyRent = monthlyRent;
        this.totalPremium = calculatePremium(monthlyRent, coverageMonths);
        this.policyHolder = policyHolder;
        this.insured = insured;
        this.beneficiary = beneficiary;
    }

    // ------------------------------------------------------------------
    // Domain Logic & Invariants
    // ------------------------------------------------------------------

    public static BigDecimal calculatePremium(BigDecimal monthlyRent, Integer coverageMonths) {
        if (monthlyRent == null || coverageMonths == null) {
            return BigDecimal.ZERO;
        }
        return monthlyRent.multiply(BigDecimal.valueOf(coverageMonths)).setScale(2, RoundingMode.HALF_UP);
    }

    public void addRisk(Risk risk) {
        this.risks.add(risk);
        risk.setPolicy(this);
    }

    /**
     * Cancels the policy and cascades the cancellation to all its associated risks.
     * Emits a {@link PolicyCancelledEvent}.
     */
    public void cancel() {
        this.status = PolicyStatus.CANCELLED;
        for (Risk r : this.risks) {
            r.setStatus(RiskStatus.CANCELLED);
        }
        registerEvent(new PolicyCancelledEvent(this.id));
    }

    /**
     * Renews the policy applying an official IPC index increase.
     * Recalculates monthly rent and total premium, shifts coverage dates,
     * and emits a {@link PolicyRenewedEvent}.
     */
    public void renew(BigDecimal ipc) {
        if (this.status == PolicyStatus.CANCELLED) {
            throw new IllegalStateException("Cannot renew a cancelled policy.");
        }

        BigDecimal previousRent = this.monthlyRent;
        // Formula: newRent = currentRent * (1 + (ipc / 100))
        BigDecimal factor = BigDecimal.ONE.add(
                ipc.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );
        this.monthlyRent = this.monthlyRent.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        this.totalPremium = calculatePremium(this.monthlyRent, this.coverageMonths);
        this.status = PolicyStatus.RENEWED;

        // Shift coverage dates forward
        LocalDate newStartDate = this.endDate.plusDays(1);
        this.startDate = newStartDate;
        this.endDate = newStartDate.plusMonths(this.coverageMonths).minusDays(1);

        registerEvent(new PolicyRenewedEvent(this.id, previousRent, this.monthlyRent, ipc));
    }

    // ------------------------------------------------------------------
    // Domain Events Handling
    // ------------------------------------------------------------------

    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> drainEvents() {
        if (domainEvents.isEmpty()) {
            return Collections.emptyList();
        }
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    // ------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public PolicyType getType() {
        return type;
    }

    public void setType(PolicyType type) {
        this.type = type;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCoverageMonths() {
        return coverageMonths;
    }

    public void setCoverageMonths(Integer coverageMonths) {
        this.coverageMonths = coverageMonths;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public BigDecimal getTotalPremium() {
        return totalPremium;
    }

    public void setTotalPremium(BigDecimal totalPremium) {
        this.totalPremium = totalPremium;
    }

    public String getPolicyHolder() {
        return policyHolder;
    }

    public void setPolicyHolder(String policyHolder) {
        this.policyHolder = policyHolder;
    }

    public String getInsured() {
        return insured;
    }

    public void setInsured(String insured) {
        this.insured = insured;
    }

    public String getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public List<Risk> getRisks() {
        return risks;
    }
}
