package com.seguros.polizas.infrastructure.event;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * RabbitMQ messaging configuration (active under the {@code rabbit} profile).
 *
 * <p>Declares the topic exchange, the durable queue and the binding that routes
 * domain events by their event type. Also configures JSON serialization for
 * payloads so events travel as structured JSON.</p>
 *
 * <p>Because {@code RabbitAutoConfiguration} is excluded by default, this class
 * defines all required Rabbit beans (connection factory, admin and template)
 * explicitly so they only exist when the broker integration is requested.</p>
 */
@Configuration
@Profile("rabbit")
@EnableRabbit
public class RabbitConfig {

    public static final String EXCHANGE = "seguros.polizas.events";
    public static final String QUEUE = "seguros.polizas.events.queue";

    @Bean
    public ConnectionFactory rabbitConnectionFactory(
            @Value("${spring.rabbitmq.host:localhost}") String host,
            @Value("${spring.rabbitmq.port:5672}") int port,
            @Value("${spring.rabbitmq.username:guest}") String username,
            @Value("${spring.rabbitmq.password:guest}") String password) {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    public TopicExchange policyEventsExchange(
            @Value("${app.messaging.rabbit.exchange:" + EXCHANGE + "}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue policyEventsQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding policyEventsBinding(TopicExchange exchange, Queue queue) {
        // Routing key "#" enruta todos los eventos de dominio a la cola.
        return BindingBuilder.bind(queue).to(exchange).with("#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
