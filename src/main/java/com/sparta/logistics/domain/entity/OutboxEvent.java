package com.sparta.logistics.domain.entity;

import com.sparta.logistics.domain.model.OutboxStatus;
import com.sparta.logistics.infrastructure.persistence.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(name = "p_outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 255;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, columnDefinition = "UUID")
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, length = 100)
    private String exchange;

    @Column(name = "routing_key", nullable = false, length = 100)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message", length = MAX_ERROR_MESSAGE_LENGTH)
    private String errorMessage;

    @Column(name = "published_at")
    private Instant publishedAt;

    public static OutboxEvent create(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String exchange,
            String routingKey,
            String payload
    ) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.aggregateType = aggregateType;
        outboxEvent.aggregateId = aggregateId;
        outboxEvent.eventType = eventType;
        outboxEvent.exchange = exchange;
        outboxEvent.routingKey = routingKey;
        outboxEvent.payload = payload;
        outboxEvent.status = OutboxStatus.PENDING;
        outboxEvent.retryCount = 0;
        return outboxEvent;
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = Instant.now();
        this.errorMessage = null;
    }

    public void markPublishFailed(String errorMessage, int maxRetryCount) {
        this.retryCount++;
        this.errorMessage = truncate(errorMessage);

        if (this.retryCount >= maxRetryCount) {
            this.status = OutboxStatus.FAILED;
        }
    }

    private String truncate(String message) {
        if (message == null || message.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return message;
        }

        return message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
