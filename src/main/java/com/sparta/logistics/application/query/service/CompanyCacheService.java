package com.sparta.logistics.application.query.service;

import com.sparta.logistics.application.query.dto.company.CompanyListResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import com.sparta.logistics.domain.entity.Company;
import com.sparta.logistics.domain.repository.company.CompanyQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
/**
 * 조건 없는 기본 목록조회 결과를 캐싱한다. (Cache-Aside)
 * 키가 고정되어 있어(default) 조건 없는 요청은 전부 이 캐시를 공유한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyCacheService {
    private final CompanyQueryRepository companyQueryRepository;

    @Cacheable(value = "companyList", key = "'default'")
    public CompanyListResponseDto getDefaultList() {
        log.info("[캐시 미스] DB에서 업체 목록 조회 실행");
        Page<Company> companyPage = companyQueryRepository.search(
                new CompanySearchRequestDto(null, null, null),
                PageRequest.of(0, 10)
        );
        return CompanyListResponseDto.from(companyPage);
    }
}
