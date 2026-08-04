package com.sparta.logistics.application.command.usecase;

import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;
import com.sparta.logistics.application.command.dto.company.CompanyUpdateRequestDto;

import java.util.UUID;

public interface UpdateCompanyUseCase {
    CompanyResponseDto updateCompany(UUID id, CompanyUpdateRequestDto request);
}
