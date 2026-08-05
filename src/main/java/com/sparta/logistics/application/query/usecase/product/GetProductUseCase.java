package com.sparta.logistics.application.query.usecase.product;

import com.sparta.logistics.application.query.dto.product.ProductDetailResponseDto;

import java.util.UUID;

public interface GetProductUseCase {
    ProductDetailResponseDto get(UUID id);
}
