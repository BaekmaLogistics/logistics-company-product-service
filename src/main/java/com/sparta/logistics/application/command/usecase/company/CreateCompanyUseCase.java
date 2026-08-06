package com.sparta.logistics.application.command.usecase.company;

import com.sparta.logistics.application.command.dto.company.CompanyCreateRequestDto;
import com.sparta.logistics.application.command.dto.company.CompanyResponseDto;

public interface CreateCompanyUseCase {
    CompanyResponseDto create(CompanyCreateRequestDto request);
}
