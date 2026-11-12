package com.seguros.polizas.infrastructure.persistence.entity;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.enums.RiskStatus;
import com.seguros.polizas.domain.model.Policy;
import com.seguros.polizas.domain.model.Risk;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA Entity for Policy persistence.
 *
 * <p>Separates the pure domain model from database relational concerns.
 * Explicit mapping to/from {@link Policy} prevents JPA annotations from leaking into the domain.</p>
 */
@Entity
@Table(name = "policies")
public class PolicyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true)
    private String policyNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PolicyType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PolicyStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "coverage_months", nullable = false)
    private Integer coverageMonths;

    @Column(name = "monthly_rent", nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "total_premium", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPremium;

    @Column(name = "policy_holder")
    private String policyHolder;

    @Column(name = "insured")
    private String insured;

    @Column(name = "beneficiary")
    private String beneficiary;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RiskJpaEntity> risks = new ArrayList<>();

    public PolicyJpaEntity() {
    }

    public static PolicyJpaEntity fromDomain(Policy domain) {
        if (domain == null) return null;
        PolicyJpaEntity entity = new PolicyJpaEntity();
        entity.setId(domain.getId());
        entity.setPolicyNumber(domain.getPolicyNumber());
        entity.setType(domain.getType());
        entity.setStatus(domain.getStatus());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setCoverageMonths(domain.getCoverageMonths());
        entity.setMonthlyRent(domain.getMonthlyRent());
        entity.setTotalPremium(domain.getTotalPremium());
        entity.setPolicyHolder(domain.getPolicyHolder());
        entity.setInsured(domain.getInsured());
        entity.setBeneficiary(domain.getBeneficiary());

        if (domain.getRisks() != null) {
            for (Risk r : domain.getRisks()) {
                RiskJpaEntity riskEntity = RiskJpaEntity.fromDomain(r);
                riskEntity.setPolicy(entity);
                entity.getRisks().add(riskEntity);
            }
        }
        return entity;
    }

    public Policy toDomain() {
        Policy policy = new Policy(
                this.policyNumber,
                this.type,
                this.status,
                this.startDate,
                this.endDate,
                this.coverageMonths,
                this.monthlyRent,
                this.policyHolder,
                this.insured,
                this.beneficiary
        );
        policy.setId(this.id);
        policy.setTotalPremium(this.totalPremium);

        if (this.risks != null) {
            for (RiskJpaEntity riskEntity : this.risks) {
                Risk r = new Risk();
                r.setId(riskEntity.getId());
                r.setPolicy(policy);
                r.setStatus(riskEntity.getStatus());
                r.setPropertyAddress(riskEntity.getPropertyAddress());
                r.setCity(riskEntity.getCity());
                r.setInsuredName(riskEntity.getInsuredName());
                r.setInsuredAmount(riskEntity.getInsuredAmount());
                r.setInclusionDate(riskEntity.getInclusionDate());
                r.setCancellationDate(riskEntity.getCancellationDate());
                policy.addRisk(r);
            }
        }
        return policy;
    }

    // Getters and Setters

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

    public List<RiskJpaEntity> getRisks() {
        return risks;
    }

    public void setRisks(List<RiskJpaEntity> risks) {
        this.risks = risks;
    }
}
