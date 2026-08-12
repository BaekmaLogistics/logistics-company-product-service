package com.sparta.logistics.application.query.usecase.product;

import com.sparta.logistics.application.query.dto.product.ProductListResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.domain.model.UserRole;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetProductListUseCase {
    ProductListResponseDto getList(ProductSearchRequestDto condition, Pageable pageable, UUID userId, UserRole role);
}
