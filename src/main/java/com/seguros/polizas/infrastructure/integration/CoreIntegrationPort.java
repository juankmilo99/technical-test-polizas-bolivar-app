package com.seguros.polizas.infrastructure.integration;

/**
 * Contrato del adapter de integración con el CORE asegurador legado.
 * Actúa como Anti-Corruption Layer (ACL): traduce los eventos del dominio
 * moderno al formato esperado por WebLogic.
 */
public interface CoreIntegrationPort {

    /**
     * Envía un evento de negocio al sistema CORE legado.
     *
     * @param evento    tipo descriptivo del evento (ej. RENOVACION).
     * @param polizaId  identificador de la póliza afectada.
     */
    void notificarEvento(String evento, Long polizaId);
}
