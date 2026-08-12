package com.sparta.logistics.domain.repository.product;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static com.sparta.logistics.domain.entity.QCompany.company;
import static com.sparta.logistics.domain.entity.QProduct.product;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryImpl  implements ProductQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSearchResult> search(ProductSearchRequestDto condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.name() != null) {
            builder.and(product.name.containsIgnoreCase(condition.name()));
        }
        if (condition.companyName() != null) {
            // companyName은 Product 테이블에 없는 필드라 Company와 join하여 검색
            builder.and(company.name.containsIgnoreCase(condition.companyName()));
        }

        builder.and(product.deletedAt.isNull());
        builder.and(company.deletedAt.isNull());
        // Company와 join하여 companyName까지 한 번에 조회 (N+1 방지)
        List<ProductSearchResult> content = queryFactory
                .select(Projections.constructor(
                        ProductSearchResult.class,
                        product.id,
                        product.name,
                        product.companyId,
                        company.name,
                        product.createdAt,
                        product.updatedAt
                ))
                .from(product)
                .join(company).on(product.companyId.eq(company.id))
                .where(builder)
                .orderBy(getOrderSpecifier(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리도 동일하게 join 유지 필요
        // (builder에 companyName 조건이 있는데 join이 빠지면 company를 참조할 대상이 없어 오류 발생)
        Long total = queryFactory
                .select(product.count())
                .from(product)
                .join(company).on(product.companyId.eq(company.id))
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);

    }
    /**
     * Pageable의 Sort 정보를 QueryDSL OrderSpecifier로 변환한다.
     * sort 파라미터가 없는 경우 기본값(생성일 내림차순)으로 처리한다.
     */
    private OrderSpecifier<?> getOrderSpecifier (Pageable pageable){
        if (pageable.getSort().isEmpty()) {
            return product.createdAt.desc();
        }

        Sort.Order order = pageable.getSort().iterator().next();
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        return switch (order.getProperty()) {
            case "updatedAt" -> new OrderSpecifier<>(direction, product.updatedAt);
            default -> new OrderSpecifier<>(direction, product.createdAt);
        };
    }

}
