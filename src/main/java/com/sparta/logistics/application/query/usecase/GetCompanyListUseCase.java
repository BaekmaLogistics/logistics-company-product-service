package com.sparta.logistics.application.query.usecase;

import com.sparta.logistics.application.query.dto.company.CompanyListResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import org.springframework.data.domain.Pageable;


public interface GetCompanyListUseCase {
    CompanyListResponseDto getCompanyList(CompanySearchRequestDto condition, Pageable pageable);
}
