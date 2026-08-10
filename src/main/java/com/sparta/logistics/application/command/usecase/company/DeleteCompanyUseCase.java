package com.sparta.logistics.application.command.usecase.company;

import com.sparta.logistics.domain.model.UserRole;

import java.util.UUID;

public interface DeleteCompanyUseCase {
    void delete(UUID id,  UUID userId, UserRole role);
}