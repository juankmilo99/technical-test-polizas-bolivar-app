package com.seguros.polizas.infrastructure.persistence.entity;

import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.domain.model.Risk;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA Entity for Risk persistence.
 */
@Entity
@Table(name = "risks")
public class RiskJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyJpaEntity policy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RiskStatus status;

    @Column(name = "property_address")
    private String propertyAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "insured_name")
    private String insuredName;

    @Column(name = "insured_amount", precision = 15, scale = 2)
    private BigDecimal insuredAmount;

    @Column(name = "inclusion_date")
    private LocalDate inclusionDate;

    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    public RiskJpaEntity() {
    }

    public static RiskJpaEntity fromDomain(Risk domain) {
        if (domain == null) return null;
        RiskJpaEntity entity = new RiskJpaEntity();
        entity.setId(domain.getId());
        entity.setStatus(domain.getStatus());
        entity.setPropertyAddress(domain.getPropertyAddress());
        entity.setCity(domain.getCity());
        entity.setInsuredName(domain.getInsuredName());
        entity.setInsuredAmount(domain.getInsuredAmount());
        entity.setInclusionDate(domain.getInclusionDate());
        entity.setCancellationDate(domain.getCancellationDate());
        return entity;
    }

    public Risk toDomain() {
        Risk risk = new Risk();
        risk.setId(this.id);
        risk.setStatus(this.status);
        risk.setPropertyAddress(this.propertyAddress);
        risk.setCity(this.city);
        risk.setInsuredName(this.insuredName);
        risk.setInsuredAmount(this.insuredAmount);
        risk.setInclusionDate(this.inclusionDate);
        risk.setCancellationDate(this.cancellationDate);
        return risk;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PolicyJpaEntity getPolicy() {
        return policy;
    }

    public void setPolicy(PolicyJpaEntity policy) {
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
