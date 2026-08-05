package com.sparta.logistics.presentation.command.controller;

import com.sparta.logistics.application.command.dto.company.CompanyCreateRequestDto;
import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;
import com.sparta.logistics.application.command.dto.company.CompanyUpdateRequestDto;
import com.sparta.logistics.application.command.usecase.CreateCompanyUseCase;
import com.sparta.logistics.application.command.usecase.DeleteCompanyUseCase;
import com.sparta.logistics.application.command.usecase.UpdateCompanyUseCase;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponseCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyCommandController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeleteCompanyUseCase deleteCompanyUseCase;

    // TODO : 권한 체크(MASTER, HUB_MANAGER) - 게이트웨이 인증 / 인가 방식 확정 후 추가

    @PostMapping
    public ResponseEntity<GeneralResponse<CompanyResponseDto>> createCompany(
            @Valid @RequestBody CompanyCreateRequestDto request) {
        CompanyResponseDto response = createCompanyUseCase.createCompany(request);

        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_CREATED, response);
    }

    @PatchMapping("/{companyId}")
    public ResponseEntity<GeneralResponse<CompanyResponseDto>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyUpdateRequestDto request) {
        CompanyResponseDto response = updateCompanyUseCase.updateCompany(companyId, request);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_UPDATED, response);
    }


    @DeleteMapping("/{companyId}")
    public ResponseEntity<GeneralResponse<Void>> deleteCompany(@PathVariable UUID companyId) {
        deleteCompanyUseCase.deleteCompany(companyId);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_DELETED, null);
    }

}
