package com.sparta.logistics.application.command.usecase.product;

import com.sparta.logistics.application.command.dto.product.ProductCreateRequestDto;
import com.sparta.logistics.application.command.dto.product.ProductResponseDto;

public interface CreateProductUseCase {
    ProductResponseDto create(ProductCreateRequestDto request);
}
