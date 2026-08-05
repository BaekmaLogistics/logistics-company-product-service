package com.sparta.logistics.domain.entity;

import com.sparta.logistics.infrastructure.persistence.jpa.entity.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "p_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseUpdatableEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Builder
    private Product(String name, UUID companyId) {
        this.name = name;
        this.companyId = companyId;
    }

    public static Product create(String name, UUID companyId) {
        return Product.builder()
                .name(name)
                .companyId(companyId)
                .build();
    }

    public void update(String name) {
        if(name != null) {
            this.name = name;
        }
    }
}

