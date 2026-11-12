package com.seguros.polizas.infrastructure.integration;

import com.seguros.polizas.infrastructure.web.rest.dto.CoreEventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Outbound Anti-Corruption Layer (ACL) adapter connecting to the legacy insurance CORE.
 *
 * <p>Translates modern domain events into legacy contracts and manages HTTP communication.
 * If the legacy system is unavailable, failures are gracefully handled and logged without
 * breaking the local transaction.</p>
 */
@Component
public class CoreIntegrationAdapter implements CoreIntegrationPort {

    private static final Logger log = LoggerFactory.getLogger(CoreIntegrationAdapter.class);

    private final RestTemplate restTemplate;
    private final String mockUrl;
    private final String apiKeyHeader;
    private final String apiKeyValue;

    public CoreIntegrationAdapter(RestTemplate restTemplate,
                                  @Value("${app.core.mock-url:http://localhost:8080/api/v1/core-mock/event}") String mockUrl,
                                  @Value("${app.security.api-key-header:x-api-key}") String apiKeyHeader,
                                  @Value("${app.security.api-key-value:123456}") String apiKeyValue) {
        this.restTemplate = restTemplate;
        this.mockUrl = mockUrl;
        this.apiKeyHeader = apiKeyHeader;
        this.apiKeyValue = apiKeyValue;
    }

    @Override
    public void notifyEvent(String eventType, Long policyId) {
        CoreEventRequest request = new CoreEventRequest(eventType, policyId);

        log.info("[CORE-ACL] >> Dispatching business event to legacy WebLogic CORE: event='{}', policyId={}",
                eventType, policyId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(apiKeyHeader, apiKeyValue);

            HttpEntity<CoreEventRequest> entity = new HttpEntity<>(request, headers);
            restTemplate.postForObject(mockUrl, entity, String.class);

            log.info("[CORE-ACL] << Confirmation successfully received from CORE service for policyId={}", policyId);
        } catch (Exception ex) {
            log.warn("[CORE-ACL] !! Graceful degradation: Event logged for policyId={}, event='{}'. Reason: {}",
                    policyId, eventType, ex.getMessage());
        }
    }
}
