package com.sparta.logistics.domain.repository.product;

import com.sparta.logistics.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByIdAndDeletedAtIsNull(UUID productId);
    List<Product> findAllByCompanyIdAndDeletedAtIsNull(UUID companyId);
    // 여러 companyId를 한 번에 조회 (N+1 방지)
    List<Product> findAllByCompanyIdInAndDeletedAtIsNull(List<UUID> companyIds);
    List<Product> findAllByIdInAndDeletedAtIsNull(List<UUID> ids);
}
