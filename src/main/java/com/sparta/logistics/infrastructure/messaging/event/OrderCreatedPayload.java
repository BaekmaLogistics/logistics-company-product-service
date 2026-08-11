package com.sparta.logistics.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedPayload(
        UUID id, // 주문 ID
        UUID departureHubId, // 배송 출발 Hub ID
        UUID receiverCompanyId, // 수령 업체 ID
        UUID productId, // 상품 ID
        int quantity, // 주문 수량
        UUID deliveryId, // 배송 ID
        String orderStatus, // PENDING, DELIVERY_REQUESTED, DELIVERING, COMPLETED, CANCELED, FAILED
        String requestMessage, // 요청사항
        Instant dueDate, // 납품 기한 일시
        Instant canceledAt, // 주문 취소 일시
        String canceledReason, // 주문 취소 사유
        Instant occurredAt
) {

}