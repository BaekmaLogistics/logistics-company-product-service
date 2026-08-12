package com.sparta.logistics.application.query.usecase.product;

import com.sparta.logistics.application.query.dto.product.PopularProductResponseDto;
import com.sparta.logistics.domain.model.UserRole;

import java.util.List;
import java.util.UUID;

public interface GetPopularProductsUseCase  {
    List<PopularProductResponseDto> getPopularProducts(int limit, UUID userId, UserRole role);
}
