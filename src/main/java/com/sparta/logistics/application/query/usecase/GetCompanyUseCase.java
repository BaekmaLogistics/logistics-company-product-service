package com.sparta.logistics.application.query.usecase;

import com.sparta.logistics.application.query.dto.company.CompanyDetailResponseDto;

import java.util.UUID;

public interface GetCompanyUseCase {
    CompanyDetailResponseDto getCompany(UUID id);
}
