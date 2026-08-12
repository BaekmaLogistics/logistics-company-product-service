package com.sparta.logistics.presentation.command.controller;

import com.sparta.logistics.application.command.dto.company.CompanyCreateRequestDto;
import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;
import com.sparta.logistics.application.command.dto.company.CompanyUpdateRequestDto;
import com.sparta.logistics.application.command.usecase.company.CreateCompanyUseCase;
import com.sparta.logistics.application.command.usecase.company.DeleteCompanyUseCase;
import com.sparta.logistics.application.command.usecase.company.UpdateCompanyUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.domain.model.UserRole;
import com.sparta.logistics.presentation.common.constant.HeaderConstants;
import com.sparta.logistics.presentation.common.constant.RoleHeaderParser;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
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

    @PostMapping
    public ResponseEntity<GeneralResponse<CompanyResponseDto>> createCompany(
            @Valid @RequestBody CompanyCreateRequestDto request,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);

    //    CompanyResponseDto response = createCompanyUseCase.create(request);
        CompanyResponseDto response = createCompanyUseCase.create(request, userId, role);

        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_CREATED, response);
    }

    @PatchMapping("/{companyId}")
    public ResponseEntity<GeneralResponse<CompanyResponseDto>> updateCompany(
            @PathVariable UUID companyId,
            @Valid @RequestBody CompanyUpdateRequestDto request,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);

        CompanyResponseDto response = updateCompanyUseCase.update(companyId, request, userId, role);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_UPDATED, response);
    }


    @DeleteMapping("/{companyId}")
    public ResponseEntity<GeneralResponse<Void>> deleteCompany(
            @PathVariable UUID companyId,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);

        deleteCompanyUseCase.delete(companyId, userId, role);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_DELETED, null);
    }

}
