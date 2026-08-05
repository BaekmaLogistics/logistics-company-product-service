package com.sparta.logistics.application.command.dto.product;

import com.sparta.logistics.domain.entity.Product;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponseDto (
        UUID id,
        String name,
        UUID companyId,
        Instant createdAt,
        Instant updatedAt
){
    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getCompanyId(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

}
