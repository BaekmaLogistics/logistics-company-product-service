package com.sparta.logistics.presentation.query.controller;

import com.sparta.logistics.application.query.dto.company.CompanyDetailResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanyListResponseDto;
import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import com.sparta.logistics.application.query.usecase.company.GetCompanyListUseCase;
import com.sparta.logistics.application.query.usecase.company.GetCompanyUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
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

        CompanyDetailResponseDto response = getCompanyUseCase.get(companyId);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_FOUND, response);
    }

    @GetMapping
    public ResponseEntity<GeneralResponse<CompanyListResponseDto>> getCompanyList(
            CompanySearchRequestDto condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable) {

        CompanyListResponseDto response = getCompanyListUseCase.getList(condition, pageable);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_LIST_FOUND, response);
    }
}
