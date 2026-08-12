package com.sparta.logistics.application.query.usecase.product;

import com.sparta.logistics.application.query.dto.product.ProductDetailResponseDto;
import com.sparta.logistics.domain.model.UserRole;

import java.util.UUID;

public interface GetProductUseCase {
    ProductDetailResponseDto get(UUID id, UUID userId, UserRole role);
}
