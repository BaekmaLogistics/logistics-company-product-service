package com.sparta.logistics.domain.repository.product;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.logistics.application.query.dto.product.ProductSearchRequestDto;
import com.sparta.logistics.application.query.dto.product.ProductSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
            builder.and(company.name.containsIgnoreCase(condition.companyName()));
        }

        builder.and(product.deletedAt.isNull());

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
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        Long total = queryFactory
                .select(product.count())
                .from(product)
                .join(company).on(product.companyId.eq(company.id))
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }
}
