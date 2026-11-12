package com.seguros.polizas.infrastructure.integration;

import com.seguros.polizas.infrastructure.web.rest.dto.CoreEventoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Adaptador (outbound) Anti-Corruption Layer hacia el CORE asegurador legado.
 *
 * <p>Traduce las operaciones del dominio al contrato del sistema legado y
 * encapsula la comunicación HTTP. Si el CORE no responde, el error se registra
 * sin hacer fallar la transacción principal (en producción iría a una cola de
 * reintentos / DLQ).</p>
 */
@Component
public class CoreIntegrationAdapter implements CoreIntegrationPort {

    private static final Logger log = LoggerFactory.getLogger(CoreIntegrationAdapter.class);

    private final RestTemplate restTemplate;
    private final String mockUrl;
    private final String apiKeyHeader;
    private final String apiKeyValue;

    public CoreIntegrationAdapter(RestTemplate restTemplate,
                                  @Value("${app.core.mock-url:http://localhost:8080/api/v1/core-mock/evento}") String mockUrl,
                                  @Value("${app.security.api-key-header:x-api-key}") String apiKeyHeader,
                                  @Value("${app.security.api-key-value:123456}") String apiKeyValue) {
        this.restTemplate = restTemplate;
        this.mockUrl = mockUrl;
        this.apiKeyHeader = apiKeyHeader;
        this.apiKeyValue = apiKeyValue;
    }

    @Override
    public void notificarEvento(String tipoEvento, Long polizaId) {
        CoreEventoRequest request = new CoreEventoRequest(tipoEvento, polizaId);

        log.info("[CORE-ACL] >> Intentando enviar evento al CORE legado WebLogic: evento='{}', polizaId={}",
                tipoEvento, polizaId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(apiKeyHeader, apiKeyValue);

            HttpEntity<CoreEventoRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.postForObject(mockUrl, entity, String.class);

            log.info("[CORE-ACL] << Confirmacion recibida con exito del servicio CORE para polizaId={}", polizaId);
        } catch (Exception ex) {
            log.warn("[CORE-ACL] !! Intento de envio al CORE registrado para polizaId={}, evento='{}'. Motivo: {}",
                    polizaId, tipoEvento, ex.getMessage());
        }
    }
}
