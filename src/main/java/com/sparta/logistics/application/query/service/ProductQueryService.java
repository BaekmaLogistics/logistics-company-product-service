package com.sparta.logistics.application.query.service;

import com.sparta.logistics.application.query.dto.product.*;
import com.sparta.logistics.application.query.usecase.product.GetPopularProductsUseCase;
import com.sparta.logistics.application.query.usecase.product.GetProductListUseCase;
import com.sparta.logistics.application.query.usecase.product.GetProductUseCase;
import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.entity.Product;
import com.sparta.logistics.domain.repository.company.CompanyRepository;
import com.sparta.logistics.domain.repository.product.ProductQueryRepository;
import com.sparta.logistics.domain.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService implements GetProductUseCase, GetProductListUseCase, GetPopularProductsUseCase {
    private static final String POPULAR_PRODUCTS_KEY = "popular:products";

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final ProductQueryRepository productQueryRepository;
    private final ProductCacheService productCacheService;
    private final RedisTemplate<String, Object> redisTemplate;


    @Override
    public ProductDetailResponseDto get(UUID id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> {
                    log.warn("존재하지 않거나 삭제된 상품 조회 시도: id={}", id);
                    return new ApiException(ErrorResponseCode.PRODUCT_NOT_FOUND);
                });

        Company company = companyRepository.findByIdAndDeletedAtIsNull(product.getCompanyId())
                .orElseThrow( ()->{
                    log.warn("상품에 연결된 업체를 찾을 수 없습니다: productId={}, companyId={}", id, product.getCompanyId());
                    return new ApiException(ErrorResponseCode.COMPANY_NOT_FOUND);
                });

        return ProductDetailResponseDto.of(product, company.getName());
    }

    @Override
    public ProductListResponseDto getList(ProductSearchRequestDto condition, Pageable pageable) {
        if (isDefaultQuery(condition, pageable)) {
            return productCacheService.getDefaultList();
        }
        return searchAndBuildResponse(condition, pageable);
    }

    /**
     * Redis Sorted Set(popular:products)에서 누적 주문 수량이 높은 순으로 상위 limit개를 조회한다.
     * 삭제된 상품은 삭제 시점에 evict되므로 이 목록에 포함되지 않는다.
     */
    @Override
    public List<PopularProductResponseDto> getPopularProducts(int limit) {
        Set<ZSetOperations.TypedTuple<Object>> top = redisTemplate.opsForZSet()
                .reverseRangeWithScores(POPULAR_PRODUCTS_KEY, 0, limit - 1);

        if (top == null || top.isEmpty()) {
            return List.of();
        }

        return top.stream()
                .map(tuple -> new PopularProductResponseDto(
                        UUID.fromString((String) tuple.getValue()),
                        tuple.getScore() != null ? tuple.getScore() : 0.0
                ))
                .toList();
    }

    private ProductListResponseDto searchAndBuildResponse(ProductSearchRequestDto condition, Pageable pageable) {
        Page<ProductSearchResult> resultPage = productQueryRepository.search(condition, pageable);
        log.info("상품 목록 조회 완료: page={}, size={}, totalElements={}",
                resultPage.getNumber(), resultPage.getSize(), resultPage.getTotalElements());
        return ProductListResponseDto.from(resultPage);
    }

    private boolean isDefaultQuery(ProductSearchRequestDto condition, Pageable pageable) {
        return condition.name() == null
                && condition.companyName() == null
                && pageable.getPageNumber() == 0
                && pageable.getPageSize() == 10;
    }
}
