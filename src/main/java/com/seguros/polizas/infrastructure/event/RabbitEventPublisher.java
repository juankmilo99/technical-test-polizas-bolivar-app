package com.seguros.polizas.infrastructure.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguros.polizas.domain.event.DomainEvent;
import com.seguros.polizas.domain.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Outbound adapter that publishes domain events to a RabbitMQ broker.
 *
 * <p>Activates only when the {@code rabbit} Spring profile is enabled. Under this
 * profile the application integrates with a real message broker (RabbitMQ) instead
 * of the in-memory {@code ApplicationEventPublisher}, matching the event bus
 * described in the high-level architecture.</p>
 *
 * <p>Events are serialized to JSON and routed by their {@link DomainEvent#eventType()}
 * to the configured exchange. This keeps the domain decoupled from messaging
 * infrastructure through the {@link EventPublisher} port.</p>
 */
@Component
@Profile("rabbit")
public class RabbitEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate,
                                ObjectMapper objectMapper,
                                @Value("${app.messaging.rabbit.exchange:seguros.polizas.events}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
    }

    @Override
    public void publish(DomainEvent event) {
        String routingKey = event.eventType();
        try {
            String payload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            log.info("[RABBIT] >> Domain event published to exchange '{}' with routing key '{}': {}",
                    exchange, routingKey, payload);
        } catch (JsonProcessingException ex) {
            log.error("[RABBIT] !! Failed to serialize domain event of type '{}': {}",
                    event.eventType(), ex.getMessage());
            // In production this would be enqueued to a retry/DLQ mechanism.
        }
    }
}
