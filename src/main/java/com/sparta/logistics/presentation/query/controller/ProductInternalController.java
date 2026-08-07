package com.sparta.logistics.presentation.query.controller;

import com.sparta.logistics.application.query.dto.product.ProductDetailResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductListResponseDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.application.query.usecase.product.GetProductListUseCase;
import com.sparta.logistics.application.query.usecase.product.GetProductUseCase;
import com.sparta.logistics.common.code.GeneralResponseCode;
import com.sparta.logistics.presentation.common.dto.response.GeneralResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/api/v1/products")
@RequiredArgsConstructor
public class ProductInternalController {

    private final GetProductUseCase getProductUseCase;

    @GetMapping("/{productId}")
    public ResponseEntity<GeneralResponse<ProductDetailResponseDto>> getProduct(
            @PathVariable UUID productId) {

        ProductDetailResponseDto response = getProductUseCase.get(productId);
        return GeneralResponse.toResponseEntity(GeneralResponseCode.PRODUCT_FOUND, response);
    }
}
