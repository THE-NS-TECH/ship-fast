package com.shipfast.messaging;

import com.shipfast.config.RabbitMQConfig;
import com.shipfast.domain.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TripEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TripEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public TripEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OutboxEvent event) {
        String routingKey = getRoutingKey(event.getEventType());
        
        Message message = MessageBuilder.withBody(event.getPayload().getBytes())
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(event.getEventId())
                .setHeader("aggregateId", event.getAggregateId())
                .setHeader("eventType", event.getEventType())
                .build();

        log.debug("Publishing event {} to exchange {} with routing key {}", 
                event.getEventId(), RabbitMQConfig.EXCHANGE_NAME, routingKey);

        rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
    }

    private String getRoutingKey(String eventType) {
        if ("TRIP_CREATED".equals(eventType)) {
            return "trip.created";
        } else if ("TRIP_COMPLETED".equals(eventType)) {
            return "trip.completed";
        }
        return "trip.unknown";
    }
}
