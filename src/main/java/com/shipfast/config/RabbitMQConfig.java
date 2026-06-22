package com.shipfast.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "shipfast.trips";
    public static final String QUEUE_DISPATCH = "trips.dispatch";
    public static final String QUEUE_DLQ = "trips.dispatch.dlq";

    @Bean
    public TopicExchange tripsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue dispatchQueue() {
        return QueueBuilder.durable(QUEUE_DISPATCH)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", QUEUE_DLQ)
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding dispatchBinding(Queue dispatchQueue, TopicExchange tripsExchange) {
        return BindingBuilder.bind(dispatchQueue).to(tripsExchange).with("trip.*");
    }
}
