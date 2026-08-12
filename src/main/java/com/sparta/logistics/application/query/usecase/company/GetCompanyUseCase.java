package com.sparta.logistics.application.query.usecase.company;

import com.sparta.logistics.application.query.dto.company.CompanyDetailResponseDto;

import java.util.UUID;

public interface GetCompanyUseCase {
    CompanyDetailResponseDto get(UUID id);
}
