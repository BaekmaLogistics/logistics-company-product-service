package com.sparta.logistics.application.command.service;


import com.sparta.logistics.application.command.dto.company.CompanyCreateRequestDto;
import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;
import com.sparta.logistics.application.command.dto.company.CompanyUpdateRequestDto;
import com.sparta.logistics.application.command.usecase.company.CreateCompanyUseCase;
import com.sparta.logistics.application.command.usecase.company.DeleteCompanyUseCase;
import com.sparta.logistics.application.command.usecase.company.UpdateCompanyUseCase;
import com.sparta.logistics.application.common.AuthorizationChecker;
import com.sparta.logistics.application.common.DistributedLockExecutor;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyCommandService implements CreateCompanyUseCase, UpdateCompanyUseCase, DeleteCompanyUseCase {

    private final CompanyRepository companyRepository;
    //private final HubClient hubClient;
    private final ProductRepository productRepository;
    private final AuthorizationChecker authorizationChecker;
    private final HubValidator hubValidator;
    private final DistributedLockExecutor distributedLockExecutor;

    @CacheEvict(value = "companyList", key = "'default'")
    @Override
    public CompanyResponseDto create(CompanyCreateRequestDto request, UUID userId, UserRole role) {

        // 생성은 MASTER / HUB_MASTER만 가능 - role만으로 판단가능
        if(role != UserRole.MASTER && role != UserRole.HUB_MANAGER) {
            log.warn("업체 생성 권한 없음: userId={}, role={}", userId, role);
            throw new ApiException(ErrorResponseCode.COMPANY_ACCESS_DENIED);
        }

        // hubId 존재 검증 ( Hub 서비스에 FeignClient로 확인 )
//        try{
//            hubClient.getHub(request.hubId());
//        } catch (FeignApiException e) {
//            log.warn("업체 생성 실패 - 존재하지 않는 허브: hubId={}", request.hubId());
//            throw new ApiException(ErrorResponseCode.HUB_NOT_FOUND);
//        }

        // hubId가 실제 존재하는 허브인지 검증 (내부적으로 Resilience4j 재시도 + 실패 시 예외 변환까지 처리됨)
        hubValidator.validateHub(request.hubId());

        String lockKey = "lock:company:name:" + request.name();

        return distributedLockExecutor.executeWithLock(lockKey, () -> {
            // 같은 이름이 있는지 조회 후 있으면 오류 메시지 검출
            if (companyRepository.existsByNameAndDeletedAtIsNull(request.name())) {
                log.warn("업체명 중복으로 생성 실패: name={}", request.name());
                throw new ApiException(ErrorResponseCode.COMPANY_NAME_DUPLICATED);
            }

            Company company = Company.create(
                    request.name(),
                    request.type(),
                    request.hubId(),
                    request.address()
            );

            Company saved = companyRepository.save(company);
            log.info("업체 생성 완료: id={}, name={}", saved.getId(), saved.getName());
            return CompanyResponseDto.from(saved);
        });
    }

    @CacheEvict(value = "companyList", key = "'default'")
    @Override
    public CompanyResponseDto update(UUID id, CompanyUpdateRequestDto request, UUID userId, UserRole role) {
        Company company = findCompanyOrThrow(id, "수정");

        if (company.getDeletedAt() != null) {
            log.warn("삭제된 업체 수정 시도: id={}", id);
            throw new ApiException(ErrorResponseCode.COMPANY_ALREADY_DELETED);
        }

        // 권한 체크: role에 따라 분기
        // MASTER는 무조건 통과, HUB_MANAGER는 담당 허브 소속인지, SUPPLIER_MANAGER는 본인 업체인지 확인
        switch (role) {
            case MASTER -> { /* 통과 */ }
            case HUB_MANAGER -> authorizationChecker.checkHubOwnership(userId, company.getHubId());
            case SUPPLIER_MANAGER -> authorizationChecker.checkCompanyOwnership(userId, id);
            default -> throw new ApiException(ErrorResponseCode.COMPANY_ACCESS_DENIED);
        }

//        if(request.hubId() != null) {
//            try{
//                hubClient.getHub(request.hubId());
//            } catch (FeignApiException e) {
//                log.warn("업체 수정 실패 - 존재하지 않는 허브: hubId={}", request.hubId());
//                throw new ApiException(ErrorResponseCode.HUB_NOT_FOUND);
//            }
//        }

        // hubId를 새로 요청한 경우에만 존재 여부 검증 (null이면 hubId 변경 없이 name/address만 수정하는 요청)
        if (request.hubId() != null) {
            hubValidator.validateHub(request.hubId());
        }

        company.update(request.name(), request.address(), request.hubId());
        log.info("업체 수정 완료: id={}, name = {}, address = {}, hub_id = {}",
                id, request.name(), request.address(), request.hubId());

        return CompanyResponseDto.from(company);
    }

    @CacheEvict(value = "companyList", key = "'default'")
    @Override
    public void delete(UUID id, UUID userId, UserRole role) {
        Company company = findCompanyOrThrow(id, "삭제");

        // 삭제 시 멱등처리
        if (company.getDeletedAt() != null) {
            log.info("이미 삭제된 업체 재삭제 요청(멱등 처리): id={}", id);
            return;
        }

        // 삭제는 MASTER/HUB_MANAGER만 가능 (HUB_MANAGER는 담당 허브인지도 확인)
        switch (role) {
            case MASTER -> { /* 통과 */ }
            case HUB_MANAGER -> authorizationChecker.checkHubOwnership(userId, company.getHubId());
            default -> throw new ApiException(ErrorResponseCode.COMPANY_ACCESS_DENIED);
        }

        company.softDelete(null);

        // 소속 상품도 함께 비활성화
        List<Product> products = productRepository.findAllByCompanyIdAndDeletedAtIsNull(id);
        products.forEach(product -> product.softDelete(null));

        log.info("업체 삭제 완료: id={}", id);
    }

    /**
     * id로 업체를 조회하며, 없으면 COMPANY_NOT_FOUND 예외를 던진다.
     * actionName은 로그 메시지에 표시할 동작 이름(예: "수정", "삭제")이다.
     */
    private Company findCompanyOrThrow(UUID id, String actionName) {
        return companyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 업체 {} 시도: id={}", actionName, id);
                    return new ApiException(ErrorResponseCode.COMPANY_NOT_FOUND);
                });
    }
}
