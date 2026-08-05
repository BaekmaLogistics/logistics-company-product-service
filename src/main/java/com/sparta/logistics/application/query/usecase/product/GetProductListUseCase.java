package com.sparta.logistics.application.query.usecase.product;

import com.sparta.logistics.application.query.dto.product.ProductListResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import org.springframework.data.domain.Pageable;

public interface GetProductListUseCase {
    ProductListResponseDto getList(ProductSearchRequestDto condition, Pageable pageable);
}
