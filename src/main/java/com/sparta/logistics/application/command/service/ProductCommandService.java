package com.sparta.logistics.application.command.service;

import com.sparta.logistics.application.command.dto.product.ProductCreateRequestDto;
import com.sparta.logistics.application.command.dto.product.ProductResponseDto;
import com.sparta.logistics.application.command.dto.product.ProductUpdateRequestDto;
import com.sparta.logistics.application.command.usecase.product.CreateProductUseCase;
import com.sparta.logistics.application.command.usecase.product.DeleteProductUseCase;
import com.sparta.logistics.application.command.usecase.product.UpdateProductUseCase;
import com.sparta.logistics.application.common.AuthorizationChecker;
import com.sparta.logistics.application.common.HubValidator;
import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.entity.Product;
import com.sparta.logistics.domain.model.UserRole;
import com.sparta.logistics.domain.repository.company.CompanyRepository;
import com.sparta.logistics.domain.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
    //private final HubClient hubClient;
    private final AuthorizationChecker authorizationChecker;
    private final HubValidator hubValidator;

    @CacheEvict(value = "productList", key = "'default'")
    @Override
    public ProductResponseDto create(ProductCreateRequestDto request, UUID userId, UserRole role){
        // 소속 업체가 실제 존재하는지 검증 (없으면 COMPANY_NOT_FOUND)
//        Company company = companyRepository.findByIdAndDeletedAtIsNull(request.companyId())
//                .orElseThrow(()->{
//            log.warn("업체가 존재하지 않습니다. companyId = {}", request.companyId());
//            return new ApiException(ErrorResponseCode.COMPANY_NOT_FOUND);
//        });
        Company company = findCompanyOrThrow(request.companyId(), null);

        // 상품 생성은 MASTER/HUB_MANAGER(담당 허브)/SUPPLIER_MANAGER(본인 업체)만 가능
        switch (role) {
            case MASTER -> { /* 통과 */ }
            case HUB_MANAGER -> authorizationChecker.checkHubOwnership(userId, company.getHubId());
            case SUPPLIER_MANAGER -> authorizationChecker.checkCompanyOwnership(userId, request.companyId());
            default -> throw new ApiException(ErrorResponseCode.PRODUCT_ACCESS_DENIED);
        }

        // 업체의 허브ID로 허브 존재 검증
//        try{
//            hubClient.getHub(company.getHubId());
//        } catch (FeignException.FeignClientException e) {
//            log.warn("상품 생성 실패 - 존재하지 않는 허브: hubId={}", company.getHubId());
//            throw new ApiException(ErrorResponseCode.HUB_NOT_FOUND);
//        }

        // 업체가 속한 허브가 실제 존재하는지 검증 (Resilience4j 재시도 + 실패 시 예외 변환 포함)
        hubValidator.validateHub(company.getHubId());

        Product product = Product.create(
                request.name(),
                request.companyId()
        );

        Product saved = productRepository.save(product);
        log.info("상품 생성 완료: id={}, name={}", saved.getId(), saved.getName());
        return ProductResponseDto.from(saved);
    }

    @CacheEvict(value = "productList", key = "'default'")
    @Override
    public ProductResponseDto update(UUID id, ProductUpdateRequestDto request, UUID userId, UserRole role) {
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

        // 상품 수정은 MASTER/HUB_MANAGER(담당 허브)/SUPPLIER_MANAGER(본인 업체)만 가능
        Company company = findCompanyOrThrow(product.getCompanyId(), id);

        switch (role) {
            case MASTER -> { /* 통과 */ }
            case HUB_MANAGER -> authorizationChecker.checkHubOwnership(userId, company.getHubId());
            case SUPPLIER_MANAGER -> authorizationChecker.checkCompanyOwnership(userId, product.getCompanyId());
            default -> throw new ApiException(ErrorResponseCode.PRODUCT_ACCESS_DENIED);
        }

        // null이 아닌 필드만 갱신 (부분 수정)
        product.update(request.name());
        log.info("상품 수정 완료: id={}", id);
        return ProductResponseDto.from(product);
    }

    @CacheEvict(value = "productList", key = "'default'")
    @Override
    public void delete(UUID id, UUID userId, UserRole role) {
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

        // 삭제는 MASTER/HUB_MANAGER만 가능 (발제문 권한표 기준, SUPPLIER_MANAGER는 삭제 불가)
        Company company = findCompanyOrThrow(product.getCompanyId(), id);

        switch (role) {
            case MASTER -> { /* 통과 */ }
            case HUB_MANAGER -> authorizationChecker.checkHubOwnership(userId, company.getHubId());
            default -> throw new ApiException(ErrorResponseCode.PRODUCT_ACCESS_DENIED);
        }

        product.softDelete(null);
        log.info("상품 삭제 완료: id={}", id);
    }

    /**
     * companyId로 삭제되지 않은 업체를 조회하며, 없으면 COMPANY_NOT_FOUND 예외를 던진다.
     * productId는 로그 추적용이며, 상품이 아직 없는 생성 시점에는 null을 넘긴다.
     */
    private Company findCompanyOrThrow(UUID companyId, UUID productId) {
        return companyRepository.findByIdAndDeletedAtIsNull(companyId).orElseThrow(()->{
            log.warn("업체가 존재하지 않습니다: productId={}, companyId={}", productId, companyId);
            return new ApiException(ErrorResponseCode.COMPANY_NOT_FOUND);
        });

    }

}
