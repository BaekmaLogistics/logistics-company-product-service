package com.sparta.logistics.application.command.usecase.company;

import com.sparta.logistics.application.command.dto.company.CompanyCreateRequestDto;
import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;
import com.sparta.logistics.domain.model.UserRole;

import java.util.UUID;

public interface CreateCompanyUseCase {
    CompanyResponseDto create(CompanyCreateRequestDto request, UUID userId, UserRole role);
}
