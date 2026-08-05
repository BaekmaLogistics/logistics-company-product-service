package com.sparta.logistics.application.query.usecase.company;

import com.sparta.logistics.application.query.dto.company.CompanyListResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import org.springframework.data.domain.Pageable;


public interface GetCompanyListUseCase {
    CompanyListResponseDto getList(CompanySearchRequestDto condition, Pageable pageable);
}
