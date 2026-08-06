package com.sparta.logistics.application.command.service;


import com.sparta.logistics.application.command.dto.company.CompanyCreateRequestDto;
import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;
import com.sparta.logistics.application.command.dto.company.CompanyUpdateRequestDto;
import com.sparta.logistics.application.command.usecase.company.CreateCompanyUseCase;
import com.sparta.logistics.application.command.usecase.company.DeleteCompanyUseCase;
import com.sparta.logistics.application.command.usecase.company.UpdateCompanyUseCase;
import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.repository.company.CompanyRepository;
import com.sparta.logistics.infrastructure.feign.client.HubClient;
import com.sparta.logistics.infrastructure.feign.exception.FeignApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyCommandService implements CreateCompanyUseCase, UpdateCompanyUseCase, DeleteCompanyUseCase {

    private final CompanyRepository companyRepository;
    private final HubClient hubClient;

    @Override
    public CompanyResponseDto create(CompanyCreateRequestDto request) {
        // hubId 존재 검증 ( Hub 서비스에 FeignClient로 확인 )
        try{
            hubClient.getHub(request.hubId());
        } catch (FeignApiException e) {
            log.warn("업체 생성 실패 - 존재하지 않는 허브: hubId={}", request.hubId());
            throw new ApiException(ErrorResponseCode.HUB_NOT_FOUND);
        }

        // 같은 이름이 있는지 조회 후 있으면 오류 메시지 검출
        if(companyRepository.existsByNameAndDeletedAtIsNull(request.name())) {
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
    }

    @Override
    public CompanyResponseDto update(UUID id, CompanyUpdateRequestDto request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 업체 수정 시도: id={}", id);
                    return new ApiException(ErrorResponseCode.COMPANY_NOT_FOUND);
                });

        if (company.getDeletedAt() != null) {
            log.warn("삭제된 업체 수정 시도: id={}", id);
            throw new ApiException(ErrorResponseCode.COMPANY_ALREADY_DELETED);
        }

        // TODO : 권한 확인 ( MASTER / HUB_MANAGER / COMPANY_MANAGER )
        // TODO : 게이트웨이 인증 / 인가 방식 확정 후 헤더에서 role, userId 꺼내서 검증 로직 추가

        company.update(request.name(), request.address());
        log.info("업체 수정 완료: id={}", id);

        return CompanyResponseDto.from(company);
    }

    @Override
    public void delete(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 업체 삭제 시도: id={}", id);
                    return new ApiException(ErrorResponseCode.COMPANY_NOT_FOUND);
                });

        if (company.getDeletedAt() != null) {
            log.info("이미 삭제된 업체 재삭제 요청(멱등 처리): id={}", id);
            return;
        }

        // TODO : 권한 확인

        company.softDelete(null);
        log.info("업체 삭제 완료: id={}", id);
    }
}
