package com.seguros.polizas.domain.model;

import com.seguros.polizas.domain.enums.RiskStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Insured risk (insurable real estate property unit).
 */
public class Risk {

    private Long id;
    private Policy policy;
    private RiskStatus status;
    private String propertyAddress;
    private String city;
    private String insuredName;
    private BigDecimal insuredAmount;
    private LocalDate inclusionDate;
    private LocalDate cancellationDate;

    public Risk() {
    }

    public Risk(Policy policy, RiskStatus status, String propertyAddress, String city,
                String insuredName, BigDecimal insuredAmount) {
        this.policy = policy;
        this.status = status;
        this.propertyAddress = propertyAddress;
        this.city = city;
        this.insuredName = insuredName;
        this.insuredAmount = insuredAmount;
        this.inclusionDate = LocalDate.now();
    }

    public void cancel() {
        this.status = RiskStatus.CANCELLED;
        this.cancellationDate = LocalDate.now();
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public RiskStatus getStatus() {
        return status;
    }

    public void setStatus(RiskStatus status) {
        this.status = status;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getInsuredName() {
        return insuredName;
    }

    public void setInsuredName(String insuredName) {
        this.insuredName = insuredName;
    }

    public BigDecimal getInsuredAmount() {
        return insuredAmount;
    }

    public void setInsuredAmount(BigDecimal insuredAmount) {
        this.insuredAmount = insuredAmount;
    }

    public LocalDate getInclusionDate() {
        return inclusionDate;
    }

    public void setInclusionDate(LocalDate inclusionDate) {
        this.inclusionDate = inclusionDate;
    }

    public LocalDate getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDate cancellationDate) {
        this.cancellationDate = cancellationDate;
    }
}
