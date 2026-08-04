package com.sparta.logistics.presentation.query.controller;

import com.sparta.logistics.application.query.dto.company.CompanyDetailResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanyListResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import com.sparta.logistics.application.query.usecase.GetCompanyListUseCase;
import com.sparta.logistics.application.query.usecase.GetCompanyUseCase;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponseCode;
import com.sparta.logistics.presentation.common.util.PageableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/companies")
public class CompanyQueryController {

    private final GetCompanyUseCase getCompanyUseCase;
    private final GetCompanyListUseCase getCompanyListUseCase;

    @GetMapping("/{companyId}")
    public ResponseEntity<GeneralResponse<CompanyDetailResponseDto>> getCompany(
            @PathVariable UUID companyId) {

        CompanyDetailResponseDto response = getCompanyUseCase.getCompany(companyId);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_FOUND, response);
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<CompanyListResponseDto>> getCompanyList(
            CompanySearchRequestDto condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {

        Pageable normalized = PageableUtils.normalize(pageable);
        CompanyListResponseDto response = getCompanyListUseCase.getCompanyList(condition, normalized);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_LIST_FOUND, response);
    }
}
