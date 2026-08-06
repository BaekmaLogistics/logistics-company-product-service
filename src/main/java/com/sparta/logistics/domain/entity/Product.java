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

    // 같은 서비스 내 실제 FK (Company와 동일 DB) - 연관관계 매핑 대신 단순 UUID 컬럼으로 관리
    // (Hub처럼 다른 서비스를 참조하는 hubId와 달리, Company는 같은 서비스지만
    //  조회 시마다 자동 로딩되는 부담을 피하고자 의도적으로 @ManyToOne 사용 안 함)
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

    // 부분 수정 : null이 아닌 필드만 갱신
    public void update(String name) {
        if(name != null) {
            this.name = name;
        }
    }
}

