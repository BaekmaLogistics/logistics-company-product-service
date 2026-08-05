package com.sparta.logistics.presentation.command.controller;


import com.sparta.logistics.application.command.dto.product.ProductCreateRequestDto;
import com.sparta.logistics.application.command.dto.product.ProductResponseDto;
import com.sparta.logistics.application.command.dto.product.ProductUpdateRequestDto;
import com.sparta.logistics.application.command.usecase.product.CreateProductUseCase;
import com.sparta.logistics.application.command.usecase.product.DeleteProductUseCase;
import com.sparta.logistics.application.command.usecase.product.UpdateProductUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductCommandController {

    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;

    // 상품 등록
    @PostMapping
    public ResponseEntity<GeneralResponse<ProductResponseDto>> createProduct(
            @Valid @RequestBody ProductCreateRequestDto request) {
        ProductResponseDto response = createProductUseCase.create(request);

        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_CREATED, response);
    }

    // 상품 수정
    @PatchMapping("/{productId}")
    public ResponseEntity<GeneralResponse<ProductResponseDto>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductUpdateRequestDto request) {
        ProductResponseDto response = updateProductUseCase.update(productId, request);

        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_UPDATED, response);
    }

    // 상품 삭제
    @DeleteMapping("/{productId}")
    public ResponseEntity<GeneralResponse<Void>> deleteProduct(@PathVariable UUID productId) {

        deleteProductUseCase.delete(productId);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_DELETED, null);
    }

}
