package com.sparta.logistics.application.command.service;

import com.sparta.logistics.application.command.dto.product.ProductCreateRequestDto;
import com.sparta.logistics.application.command.dto.product.ProductResponseDto;
import com.sparta.logistics.application.command.dto.product.ProductUpdateRequestDto;
import com.sparta.logistics.application.command.usecase.product.CreateProductUseCase;
import com.sparta.logistics.application.command.usecase.product.DeleteProductUseCase;
import com.sparta.logistics.application.command.usecase.product.UpdateProductUseCase;
import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.entity.Product;
import com.sparta.logistics.domain.repository.company.CompanyRepository;
import com.sparta.logistics.domain.repository.product.ProductRepository;
import com.sparta.logistics.infrastructure.feign.client.HubClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductCommandService implements CreateProductUseCase, UpdateProductUseCase, DeleteProductUseCase {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final HubClient hubClient;

    @Override
    public ProductResponseDto create(ProductCreateRequestDto request){
        // 소속 업체가 실제 존재하는지 검증 (없으면 COMPANY_NOT_FOUND)
        Company company = companyRepository.findByIdAndDeletedAtIsNull(request.companyId())
                .orElseThrow(()->{
            log.warn("업체가 존재하지 않습니다. companyId = {}", request.companyId());
            return new ApiException(ErrorResponseCode.COMPANY_NOT_FOUND);
        });

        // 업체의 허브ID로 허브 존재 검증
        try{
            hubClient.getHub(company.getHubId());
        } catch (FeignException.FeignClientException e) {
            log.warn("상품 생성 실패 - 존재하지 않는 허브: hubId={}", company.getHubId());
            throw new ApiException(ErrorResponseCode.HUB_NOT_FOUND);
        }

        Product product = Product.create(
                request.name(),
                request.companyId()
        );

        Product saved = productRepository.save(product);
        log.info("상품 생성 완료: id={}, name={}", saved.getId(), saved.getName());
        return ProductResponseDto.from(saved);
    }

    @Override
    public ProductResponseDto update(UUID id, ProductUpdateRequestDto request) {
        // 존재하지 않는 상품이면 PRODUCT_NOT_FOUND
        Product product = productRepository.findById(id)
                .orElseThrow( () -> {
                    log.warn("존재하지 않는 상품 수정 시도: id={}", id);
                    return new ApiException(ErrorResponseCode.PRODUCT_NOT_FOUND);
                });

        // 이미 삭제된 상품은 수정 불가 (삭제와 달리 멱등 처리 아님, 명시적 에러)
        if(product.getDeletedAt() != null) {
            log.warn("삭제된 상품 수정 시도: id={}", id);
            throw new ApiException(ErrorResponseCode.PRODUCT_ALREADY_DELETED);
        }

        // null이 아닌 필드만 갱신 (부분 수정)
        product.update(request.name());
        log.info("상품 수정 완료: id={}", id);
        return ProductResponseDto.from(product);
    }


    @Override
    public void delete(UUID id) {
        // 존재한 적 없는 상품이면 PRODUCT_NOT_FOUND
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("존재하지 않은 상품 삭제 시도: id={}", id);
                    return new ApiException(ErrorResponseCode.PRODUCT_NOT_FOUND);
                });

        // 이미 삭제된 상품 재삭제 요청 → 에러 없이 조용히 종료 (멱등 처리)
        if(product.getDeletedAt() != null) {
            log.info("이미 삭제된 상품 재삭제 요청(멱등 처리): id={}", id);
            return;
        }

        product.softDelete(null);
        log.info("상품 삭제 완료: id={}", id);
    }
}
