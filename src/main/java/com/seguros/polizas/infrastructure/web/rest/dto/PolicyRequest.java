package com.seguros.polizas.infrastructure.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.seguros.polizas.domain.enums.PolicyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PolicyRequest {

    @NotBlank(message = "Policy number is mandatory")
    @JsonAlias("numeroPoliza")
    private String policyNumber;

    @NotNull(message = "Policy type is mandatory (INDIVIDUAL or COLLECTIVE)")
    @JsonAlias("tipo")
    private PolicyType type;

    @NotNull(message = "Start date is mandatory")
    @JsonAlias("fechaInicio")
    private LocalDate startDate;

    @NotNull(message = "Coverage months is mandatory")
    @Min(value = 1, message = "Coverage months must be at least 1")
    @JsonAlias("mesesVigencia")
    private Integer coverageMonths;

    @NotNull(message = "Monthly rent is mandatory")
    @DecimalMin(value = "0.01", message = "Monthly rent must be greater than zero")
    @JsonAlias("canonMensual")
    private BigDecimal monthlyRent;

    @JsonAlias("tomador")
    private String policyHolder;

    @JsonAlias("asegurado")
    private String insured;

    @JsonAlias("beneficiario")
    private String beneficiary;

    @JsonAlias("riesgoInicial")
    private RiskRequest initialRisk;

    public PolicyRequest() {
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
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

    public RiskRequest getInitialRisk() {
        return initialRisk;
    }

    public void setInitialRisk(RiskRequest initialRisk) {
        this.initialRisk = initialRisk;
    }

    // Legacy getters/setters for compatibility
    public String getNumeroPoliza() { return policyNumber; }
    public void setNumeroPoliza(String n) { this.policyNumber = n; }
    public PolicyType getTipo() { return type; }
    public void setTipo(PolicyType t) { this.type = t; }
    public LocalDate getFechaInicio() { return startDate; }
    public void setFechaInicio(LocalDate d) { this.startDate = d; }
    public Integer getMesesVigencia() { return coverageMonths; }
    public void setMesesVigencia(Integer m) { this.coverageMonths = m; }
    public BigDecimal getCanonMensual() { return monthlyRent; }
    public void setCanonMensual(BigDecimal c) { this.monthlyRent = c; }
    public String getTomador() { return policyHolder; }
    public void setTomador(String t) { this.policyHolder = t; }
    public String getAsegurado() { return insured; }
    public void setAsegurado(String a) { this.insured = a; }
    public String getBeneficiario() { return beneficiary; }
    public void setBeneficiario(String b) { this.beneficiary = b; }
    public RiskRequest getRiesgoInicial() { return initialRisk; }
    public void setRiesgoInicial(RiskRequest r) { this.initialRisk = r; }
}
