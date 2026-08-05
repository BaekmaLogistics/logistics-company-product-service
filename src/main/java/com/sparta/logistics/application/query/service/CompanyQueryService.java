package com.sparta.logistics.application.query.service;

import com.sparta.logistics.application.query.dto.company.CompanyDetailResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanyListResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import com.sparta.logistics.application.query.usecase.company.GetCompanyListUseCase;
import com.sparta.logistics.application.query.usecase.company.GetCompanyUseCase;
import com.sparta.logistics.common.code.ErrorResponseCode;
import com.sparta.logistics.common.exception.ApiException;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.repository.company.CompanyQueryRepository;
import com.sparta.logistics.domain.repository.company.CompanyRepository;
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
public class CompanyQueryService implements GetCompanyUseCase, GetCompanyListUseCase {

    private final CompanyRepository companyRepository;
    private final CompanyQueryRepository companyQueryRepository;
    @Override
    public CompanyDetailResponseDto get(UUID id) {
        Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow( ()->{
                    log.warn("존재하지 않거나 삭제된 업체 조회 시도: id={}", id);
                    return new ApiException(ErrorResponseCode.COMPANY_NOT_FOUND);
                });

        return CompanyDetailResponseDto.from(company);
    }

    @Override
    public CompanyListResponseDto getList(CompanySearchRequestDto condition, Pageable pageable) {
        Page<Company> companyPage = companyQueryRepository.search(condition, pageable);
        log.info("업체 목록 조회 완료: page={}, size={}, totalElements={}",
                companyPage.getNumber(), companyPage.getSize(), companyPage.getTotalElements());
        return CompanyListResponseDto.from(companyPage);

    }
}
