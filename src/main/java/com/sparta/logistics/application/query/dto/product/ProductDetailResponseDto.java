package com.sparta.logistics.application.query.dto.product;

import com.sparta.logistics.domain.entity.Product;

import java.time.Instant;
import java.util.UUID;

public record ProductDetailResponseDto(
        UUID id,
        String name,
        UUID companyId,
        String companyName,
        Instant createdAt,
        Instant updatedAt
) {

    public static ProductDetailResponseDto of(Product product, String companyName) {
        return new ProductDetailResponseDto(
                product.getId(),
                product.getName(),
                product.getCompanyId(),
                companyName,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
