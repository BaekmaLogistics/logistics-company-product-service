package com.sparta.logistics.presentation.query.controller;

import com.sparta.logistics.application.query.dto.product.PopularProductResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductDetailResponseDto;
import com.sparta.logistics.application.query.usecase.product.GetPopularProductsUseCase;
import com.sparta.logistics.application.query.usecase.product.GetProductUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.domain.model.UserRole;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/api/v1/products")
@RequiredArgsConstructor
public class ProductInternalController {

    private final GetProductUseCase getProductUseCase;

    @GetMapping("/{productId}")
    public ResponseEntity<GeneralResponse<ProductDetailResponseDto>> getProduct(
            @PathVariable UUID productId) {

        ProductDetailResponseDto response = getProductUseCase.get(productId, null, UserRole.MASTER);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_FOUND, response);
    }
}
