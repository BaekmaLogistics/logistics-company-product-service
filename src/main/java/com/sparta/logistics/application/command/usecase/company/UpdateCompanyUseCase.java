package com.sparta.logistics.application.command.usecase.company;

import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;
import com.sparta.logistics.application.command.dto.company.CompanyUpdateRequestDto;
import com.sparta.logistics.domain.model.UserRole;

import java.util.UUID;

public interface UpdateCompanyUseCase {
    CompanyResponseDto update(UUID id, CompanyUpdateRequestDto request, UUID userId, UserRole role);
}
