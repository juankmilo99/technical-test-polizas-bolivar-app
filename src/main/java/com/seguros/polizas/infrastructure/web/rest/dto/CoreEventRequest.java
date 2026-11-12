package com.seguros.polizas.infrastructure.web.rest.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload sent to or received by the mock WebLogic insurance CORE.
 */
public class CoreEventRequest {

    @NotBlank(message = "Event name is mandatory")
    @JsonAlias("evento")
    private String event;

    @NotNull(message = "Policy ID is mandatory")
    @JsonAlias("polizaId")
    private Long policyId;

    public CoreEventRequest() {
    }

    public CoreEventRequest(String event, Long policyId) {
        this.event = event;
        this.policyId = policyId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    // Legacy getters for backward compatibility
    public String getEvento() {
        return event;
    }

    public void setEvento(String evento) {
        this.event = evento;
    }

    public Long getPolizaId() {
        return policyId;
    }

    public void setPolizaId(Long polizaId) {
        this.policyId = polizaId;
    }
}

