package com.sparta.logistics.application.query.usecase.product;

import com.sparta.logistics.application.query.dto.product.PopularProductResponseDto;

import java.util.List;

public interface GetPopularProductsUseCase  {
    List<PopularProductResponseDto> getPopularProducts(int limit);
}
