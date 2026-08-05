package com.sparta.logistics.domain.entity;

import com.sparta.logistics.domain.model.CompanyType;
import com.sparta.logistics.infrastructure.persistence.jpa.entity.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Entity
@Table(name = "p_companies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseUpdatableEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private CompanyType type;

    // 서비스 내 FK 아님
    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Builder
    private Company(String name, CompanyType type, UUID hubId, String address) {
        this.name = name;
        this.type = type;
        this.hubId = hubId;
        this.address = address;
    }

    public static Company create(String name, CompanyType type, UUID hubId, String address) {
        return Company.builder()
                .name(name)
                .type(type)
                .hubId(hubId)
                .address(address)
                .build();
    }

    // 부분 수정 : null이 아닌 필드만 갱신
    public void update(String name, String address) {
        if(name != null)
            this.name = name;
        if(address != null)
            this.address = address;
    }
}
