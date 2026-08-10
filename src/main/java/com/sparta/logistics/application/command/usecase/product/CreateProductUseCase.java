package com.sparta.logistics.application.command.usecase.product;

import com.sparta.logistics.application.command.dto.product.ProductCreateRequestDto;
import com.sparta.logistics.application.command.dto.product.ProductResponseDto;
import com.sparta.logistics.domain.model.UserRole;

import java.util.UUID;

public interface CreateProductUseCase {
    ProductResponseDto create(ProductCreateRequestDto request, UUID userId, UserRole role);
}
