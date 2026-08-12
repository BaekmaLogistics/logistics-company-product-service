package com.sparta.logistics.domain.repository.company;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.logistics.application.query.dto.company.CompanySearchRequestDto;
import com.sparta.logistics.domain.entity.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.sparta.logistics.domain.entity.QCompany.company;

@Repository
@RequiredArgsConstructor
public class CompanyQueryRepositoryImpl implements CompanyQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Company> search(CompanySearchRequestDto condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if(condition.name() != null) {
            builder.and(company.name.containsIgnoreCase(condition.name()));
        }
        if(condition.type() != null) {
            builder.and(company.type.eq(condition.type()));
        }
        if(condition.hubId() != null) {
            builder.and(company.hubId.eq(condition.hubId()));
        }

        builder.and(company.deletedAt.isNull());

        List<Company> content = queryFactory
                .selectFrom(company)
                .where(builder)
                .orderBy(getOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(company.count())
                .from(company)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
    /**
     * Pageable의 Sort 정보를 QueryDSL OrderSpecifier로 변환한다.
     * sort 파라미터가 없는 경우 기본값(생성일 내림차순)으로 처리한다.
     */
    private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return company.createdAt.desc(); // 기본 정렬
        }

        Sort.Order order = pageable.getSort().iterator().next();
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        return switch (order.getProperty()) {
            case "updatedAt" -> new OrderSpecifier<>(direction, company.updatedAt);
            default -> new OrderSpecifier<>(direction, company.createdAt); // createdAt 및 그 외 값
        };
    }

}
