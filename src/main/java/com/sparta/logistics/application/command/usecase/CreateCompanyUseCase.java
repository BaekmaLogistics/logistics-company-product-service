package com.sparta.logistics.application.command.usecase;

import com.sparta.logistics.application.command.dto.company.CompanyCreateRequestDto;
import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;

public interface CreateCompanyUseCase {
    CompanyResponseDto createCompany(CompanyCreateRequestDto request);
}
