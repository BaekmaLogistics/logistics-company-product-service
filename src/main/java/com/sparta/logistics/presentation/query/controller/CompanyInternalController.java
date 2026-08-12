package com.sparta.logistics.presentation.query.controller;

import com.sparta.logistics.application.query.dto.company.CompanyDetailResponseDto;
import com.sparta.logistics.application.query.usecase.company.GetCompanyUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Company - 내부 API", description = "다른 서비스가 Feign으로 호출하는 업체 내부 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("internal/api/v1/companies")
public class CompanyInternalController {

    private final GetCompanyUseCase getCompanyUseCase;

    @Operation(summary = "업체 단건 조회 (내부용)", description = "서비스 간 호출 전용 엔드포인트로, 인증 헤더 없이 조회합니다.")
    @GetMapping("/{companyId}")
    public ResponseEntity<GeneralResponse<CompanyDetailResponseDto>> getCompany(
            @PathVariable UUID companyId) {

        CompanyDetailResponseDto response = getCompanyUseCase.get(companyId);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_FOUND, response);
    }
}
