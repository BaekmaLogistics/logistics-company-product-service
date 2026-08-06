package com.sparta.logistics.application.command.usecase.company;

import java.util.UUID;

public interface DeleteCompanyUseCase {
    void delete(UUID id);
}