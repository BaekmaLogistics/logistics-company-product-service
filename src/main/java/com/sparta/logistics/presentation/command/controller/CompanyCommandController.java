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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@Tag(name = "Company - 등록/수정/삭제", description = "업체 생성, 수정, 삭제 API (마스터 관리자/허브 관리자/업체 담당자 권한 필요)")
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyCommandController {

    private final CreateCompanyUseCase createCompanyUseCase;
    private final UpdateCompanyUseCase updateCompanyUseCase;
    private final DeleteCompanyUseCase deleteCompanyUseCase;

    @Operation(summary = "업체 등록", description = "새로운 업체를 등록합니다. hubId는 Hub 서비스에 존재하는 값이어야 합니다.")
    @PostMapping
    public ResponseEntity<GeneralResponse<CompanyResponseDto>> createCompany(
            @Valid @RequestBody CompanyCreateRequestDto request,
            @RequestHeader(HeaderConstants.USER_ID) UUID userId,
            @RequestHeader(HeaderConstants.USER_ROLE) String rawRole) {

        UserRole role = RoleHeaderParser.parse(rawRole);

        CompanyResponseDto response = createCompanyUseCase.create(request, userId, role);

        return GeneralResponse.toResponseEntity(GeneralResponseCode.COMPANY_CREATED, response);
    }

    @Operation(summary = "업체 수정", description = "업체 정보를 수정합니다. 허브 관리자는 담당 허브 소속 업체만, 업체 담당자는 본인 업체만 수정 가능합니다.")
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

    @Operation(summary = "업체 삭제", description = "업체를 논리 삭제합니다. 소속 상품도 함께 비활성화됩니다.")
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
