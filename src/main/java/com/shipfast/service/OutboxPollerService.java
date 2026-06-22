package com.shipfast.service;

import com.shipfast.domain.OutboxEvent;
import com.shipfast.messaging.TripEventPublisher;
import com.shipfast.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxPollerService {

    private static final Logger log = LoggerFactory.getLogger(OutboxPollerService.class);
    private final OutboxEventRepository outboxEventRepository;
    private final TripEventPublisher eventPublisher;

    public OutboxPollerService(OutboxEventRepository outboxEventRepository, TripEventPublisher eventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${shipfast.outbox.poll-interval:5000}")
    @Transactional
    public void processOutboxEvents() {
        log.debug("Polling for outbox events...");
        List<OutboxEvent> events = outboxEventRepository.findUnprocessedEvents(10);
        
        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                log.info("Processing event {}: type={}, aggregateId={}", 
                        event.getEventId(), event.getEventType(), event.getAggregateId());
                
                eventPublisher.publish(event);
                
                event.setProcessedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to process outbox event {}", event.getEventId(), e);
                // We don't throw here so that other events can be processed
            }
        }
    }
}
