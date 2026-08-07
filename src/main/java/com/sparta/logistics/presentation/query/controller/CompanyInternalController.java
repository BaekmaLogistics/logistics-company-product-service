package com.sparta.logistics.presentation.query.controller;

import com.sparta.logistics.application.query.dto.company.CompanyDetailResponseDto;
import com.sparta.logistics.application.query.usecase.company.GetCompanyUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("internal/api/v1/companies")
public class CompanyInternalController {

    private final GetCompanyUseCase getCompanyUseCase;

    @GetMapping("/{companyId}")
    public ResponseEntity<GeneralResponse<CompanyDetailResponseDto>> getCompany(
            @PathVariable UUID companyId) {

        CompanyDetailResponseDto response = getCompanyUseCase.get(companyId);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_FOUND, response);
    }
}
