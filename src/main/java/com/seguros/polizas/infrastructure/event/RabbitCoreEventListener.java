package com.seguros.polizas.infrastructure.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguros.polizas.infrastructure.integration.CoreIntegrationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Message-driven consumer (active under the {@code rabbit} profile).
 *
 * <p>Listens on the RabbitMQ queue, deserializes each domain event and forwards it
 * to the legacy CORE through the Anti-Corruption Layer. This mirrors the
 * {@code CoreEventRelay} flow but driven by a real message broker instead of an
 * in-memory application event, fully decoupling producers from consumers.</p>
 */
@Component
@Profile("rabbit")
public class RabbitCoreEventListener {

    private static final Logger log = LoggerFactory.getLogger(RabbitCoreEventListener.class);

    private final CoreIntegrationPort coreIntegrationPort;
    private final ObjectMapper objectMapper;

    public RabbitCoreEventListener(CoreIntegrationPort coreIntegrationPort, ObjectMapper objectMapper) {
        this.coreIntegrationPort = coreIntegrationPort;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onMessage(String messageBody) {
        try {
            JsonNode node = objectMapper.readTree(messageBody);
            String eventType = node.has("eventType") ? node.get("eventType").asText() : node.path("type").asText(null);
            Long aggregateId = extractAggregateId(node);
            coreIntegrationPort.notifyEvent(eventType, aggregateId);

            log.info("[RABBIT-CONSUMER] >> Event '{}' forwarded to CORE for aggregateId={}", eventType, aggregateId);
        } catch (Exception ex) {
            log.error("[RABBIT-CONSUMER] !! Error processing event from queue: {}", ex.getMessage());
        }
    }

    private Long extractAggregateId(JsonNode node) {
        for (String field : new String[]{"aggregateId", "policyId"}) {
            if (node.has(field) && !node.get(field).isNull()) {
                return node.get(field).asLong();
            }
        }
        return null;
    }
}
