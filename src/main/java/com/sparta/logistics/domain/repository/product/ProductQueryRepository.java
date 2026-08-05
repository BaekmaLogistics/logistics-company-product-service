package com.sparta.logistics.domain.repository.product;

import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {
    Page<ProductSearchResult> search(ProductSearchRequestDto condition, Pageable pageable);

}
