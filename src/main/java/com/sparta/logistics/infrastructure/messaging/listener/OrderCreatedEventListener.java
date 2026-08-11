package com.sparta.logistics.infrastructure.messaging.listener;

import com.sparta.logistics.infrastructure.messaging.envelope.EventEnvelope;
import com.sparta.logistics.infrastructure.messaging.event.OrderCreatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String POPULAR_PRODUCTS_KEY = "popular:products";

    @RabbitListener(queues = "${message.queue.company}")
    public void handleOrderCreated(EventEnvelope<OrderCreatedPayload> envelope) {
        if (!"OrderCreated".equals(envelope.header().eventType())) {
            return;
        }

        OrderCreatedPayload payload = envelope.payload();
        log.info("주문 생성 이벤트 수신: orderId={}, productId={}, quantity={}",
                payload.id(), payload.productId(), payload.quantity());

        redisTemplate.opsForZSet().incrementScore(
                POPULAR_PRODUCTS_KEY,
                payload.productId().toString(),
                payload.quantity()
        );
    }
}
