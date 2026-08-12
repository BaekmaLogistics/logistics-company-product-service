package com.sparta.logistics.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfig {
    @Value("${message.exchange}")
    private String exchange;

    @Value("${message.queue.delivery}")
    private String queueDelivery;
    @Value("${message.queue.hub}")
    private String queueHub;
    @Value("${message.queue.notification}")
    private String queueNotification;

    // 큐를 이벤트별로 분리 (하나의 큐를 여러 리스너가 공유 구독하면
    // RabbitMQ가 메시지를 컨슈머 중 하나에게만 라운드로빈으로 배분해
    // 이벤트가 확률적으로 유실되는 문제가 있어 분리함)
    @Value("${message.queue.company-hub-deleted}")
    private String queueCompanyHubDeleted;
    @Value("${message.queue.company-order-created}")
    private String queueCompanyOrderCreated;


    @Value("${message.binding-key.notification.inventory-low}")
    private String keyNotificationInventoryLow;
    @Value("${message.binding-key.notification.order-created}")
    private String keyNotificationOrderCreated;
    @Value("${message.binding-key.notification.order-canceled}")
    private String keyNotificationOrderCanceled;
    @Value("${message.binding-key.notification.order-completed}")
    private String keyNotificationOrderCompleted;
    @Value("${message.binding-key.hub.route-changed}")
    private String keyHubRouteChanged;
    @Value("${message.binding-key.company.hub-deleted}")
    private String keyCompanyHubDeleted;

    @Value("${message.binding-key.company.order-created}")
    private String keyCompanyOrderCreated;

    @Bean
    public TopicExchange exchange() { return new TopicExchange(exchange); }

    @Bean public Queue queueDelivery() { return new Queue(queueDelivery); }
    @Bean public Queue queueHub() { return new Queue(queueHub); }
    @Bean public Queue queueNotification() { return new Queue(queueNotification); }
    @Bean public Queue queueCompanyHubDeleted() { return new Queue(queueCompanyHubDeleted); }
    @Bean public Queue queueCompanyOrderCreated() { return new Queue(queueCompanyOrderCreated); }

    // Hub -> Notification (재고 부족)
    @Bean
    public Binding bindingNotificationInventoryLow() {
        return BindingBuilder.bind(queueNotification())
                .to(exchange())
                .with(keyNotificationInventoryLow);
    }

    // Order -> Notification (주문 생성)
    @Bean
    public Binding bindingNotificationOrderCreated() {
        return BindingBuilder.bind(queueNotification())
                .to(exchange())
                .with(keyNotificationOrderCreated);
    }

    // Order -> Notification (주문 취소)
    @Bean
    public Binding bindingNotificationOrderCanceled() {
        return BindingBuilder.bind(queueNotification())
                .to(exchange())
                .with(keyNotificationOrderCanceled);
    }

    // Order -> Notification (주문 완료)
    @Bean
    public Binding bindingNotificationOrderCompleted() {
        return BindingBuilder.bind(queueNotification())
                .to(exchange())
                .with(keyNotificationOrderCompleted);
    }

    // Hub -> Hub (허브 경로 변경)
    @Bean
    public Binding bindingHubRouteChanged() {
        return BindingBuilder.bind(queueHub())
                .to(exchange())
                .with(keyHubRouteChanged);
    }

    // Hub -> Company (허브 삭제) : 전용 큐로 분리
    @Bean
    public Binding bindingCompanyHubDeleted() {
        return BindingBuilder.bind(queueCompanyHubDeleted())
                .to(exchange())
                .with(keyCompanyHubDeleted);
    }

    // Order -> Company (주문 생성, 인기 상품 집계용) : 전용 큐로 분리
    @Bean
    public Binding bindingCompanyOrderCreated() {
        return BindingBuilder.bind(queueCompanyOrderCreated())
                .to(exchange())
                .with(keyCompanyOrderCreated);
    }
}
