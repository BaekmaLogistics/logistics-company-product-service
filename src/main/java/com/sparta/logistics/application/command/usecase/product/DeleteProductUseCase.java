package com.sparta.logistics.application.command.usecase.product;

import com.sparta.logistics.domain.model.UserRole;

import java.util.UUID;

public interface DeleteProductUseCase {
    void delete(UUID id, UUID userId, UserRole role);
}
