package com.sparta.logistics.application.command.usecase.product;

import java.util.UUID;

public interface DeleteProductUseCase {
    void delete(UUID id);
}
