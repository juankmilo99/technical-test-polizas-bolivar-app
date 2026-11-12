package com.seguros.polizas.infrastructure.web.rest.dto;

import com.seguros.polizas.domain.enums.PolicyStatus;
import com.seguros.polizas.domain.enums.PolicyType;
import com.seguros.polizas.domain.model.Policy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PolicyResponse {

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
    private int riskCount;
    private List<RiskResponse> risks = new ArrayList<>();

    public PolicyResponse() {
    }

    public static PolicyResponse fromEntity(Policy p) {
        if (p == null) return null;
        PolicyResponse dto = new PolicyResponse();
        dto.setId(p.getId());
        dto.setPolicyNumber(p.getPolicyNumber());
        dto.setType(p.getType());
        dto.setStatus(p.getStatus());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setCoverageMonths(p.getCoverageMonths());
        dto.setMonthlyRent(p.getMonthlyRent());
        dto.setTotalPremium(p.getTotalPremium());
        dto.setPolicyHolder(p.getPolicyHolder());
        dto.setInsured(p.getInsured());
        dto.setBeneficiary(p.getBeneficiary());

        if (p.getRisks() != null) {
            dto.setRiskCount(p.getRisks().size());
            dto.setRisks(p.getRisks().stream()
                    .map(RiskResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

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

    public int getRiskCount() {
        return riskCount;
    }

    public void setRiskCount(int riskCount) {
        this.riskCount = riskCount;
    }

    public List<RiskResponse> getRisks() {
        return risks;
    }

    public void setRisks(List<RiskResponse> risks) {
        this.risks = risks;
    }

    // Legacy getters for backward compatibility
    public String getNumeroPoliza() { return policyNumber; }
    public PolicyType getTipo() { return type; }
    public PolicyStatus getEstado() { return status; }
    public LocalDate getFechaInicio() { return startDate; }
    public LocalDate getFechaFin() { return endDate; }
    public Integer getMesesVigencia() { return coverageMonths; }
    public BigDecimal getCanonMensual() { return monthlyRent; }
    public BigDecimal getPrimaTotal() { return totalPremium; }
    public String getTomador() { return policyHolder; }
    public String getAsegurado() { return insured; }
    public String getBeneficiario() { return beneficiary; }
    public int getCantidadRiesgos() { return riskCount; }
    public List<RiskResponse> getRiesgos() { return risks; }
}

