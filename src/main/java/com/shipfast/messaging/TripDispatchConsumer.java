package com.shipfast.messaging;

import com.shipfast.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TripDispatchConsumer {

    private static final Logger log = LoggerFactory.getLogger(TripDispatchConsumer.class);
    
    // In memory store for idempotency check (in production this would be Redis or a database table)
    private final Set<String> processedMessageIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DISPATCH)
    public void consume(Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        
        if (messageId != null && !processedMessageIds.add(messageId)) {
            log.info("Message {} already processed, skipping (Idempotency)", messageId);
            return;
        }

        String eventType = message.getMessageProperties().getHeader("eventType");
        String payload = new String(message.getBody());
        
        log.info("Received event {} with ID {}: {}", eventType, messageId, payload);
        
        // Here we would dispatch to third-party logistics APIs
        // For the exercise, we just log and complete successfully.
    }
}
