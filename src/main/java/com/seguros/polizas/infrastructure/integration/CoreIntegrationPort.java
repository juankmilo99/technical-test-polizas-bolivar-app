package com.seguros.polizas.infrastructure.integration;

/**
 * Outbound port for the Anti-Corruption Layer (ACL) integrating with the legacy insurance CORE.
 * Translates modern domain events into legacy WebLogic payloads.
 */
public interface CoreIntegrationPort {

    /**
     * Notifies a business event to the legacy CORE system.
     *
     * @param event    Descriptive event type (e.g. POLICY_RENEWED, POLICY_CANCELLED).
     * @param policyId Identifier of the affected policy.
     */
    void notifyEvent(String event, Long policyId);

    default void notificarEvento(String event, Long policyId) {
        notifyEvent(event, policyId);
    }
}
