package com.sparta.logistics.application.query.service;

import com.sparta.logistics.application.query.dto.company.CompanyListResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductDetailResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductListResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchResult;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService implements GetProductUseCase, GetProductListUseCase {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final ProductQueryRepository productQueryRepository;

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
        Page<ProductSearchResult> resultPage = productQueryRepository.search(condition, pageable);
        log.info("상품 목록 조회 완료: page={}, size={}, totalElements={}",
                resultPage.getNumber(), resultPage.getSize(), resultPage.getTotalElements());
        return ProductListResponseDto.from(resultPage);
    }
}
