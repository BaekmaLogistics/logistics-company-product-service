package com.sparta.logistics.application.command.usecase.product;

import com.sparta.logistics.application.command.dto.product.ProductResponseDto;
import com.sparta.logistics.application.command.dto.product.ProductUpdateRequestDto;

import java.util.UUID;

public interface UpdateProductUseCase {
    ProductResponseDto update(UUID id, ProductUpdateRequestDto request);
}
