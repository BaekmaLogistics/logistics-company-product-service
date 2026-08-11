package com.sparta.logistics.application.query.service;

import com.sparta.logistics.application.query.dto.product.ProductListResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchResult;
import com.sparta.logistics.domain.repository.product.ProductQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
/**
 * 조건 없는 기본 목록조회 결과를 캐싱한다. (Cache-Aside)
 * 키가 고정되어 있어(default) 조건 없는 요청은 전부 이 캐시를 공유한다.
 */
@Component
@RequiredArgsConstructor
public class ProductCacheService {
    private final ProductQueryRepository productQueryRepository;

    @Cacheable(value = "productList", key = "'default'")
    public ProductListResponseDto getDefaultList() {
        Page<ProductSearchResult> resultPage = productQueryRepository.search(
                new ProductSearchRequestDto(null, null),
                PageRequest.of(0, 10)
        );
        return ProductListResponseDto.from(resultPage);
    }
}
