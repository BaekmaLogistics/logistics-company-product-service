package com.sparta.logistics.domain.repository.product;

import com.sparta.logistics.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndDeletedAtIsNull(UUID productId);
    List<Product> findAllByCompanyIdAndDeletedAtIsNull(UUID companyId);
}
