package com.dugaza.letsdrive.repository.review

import com.dugaza.letsdrive.entity.common.QReview
import com.dugaza.letsdrive.entity.common.Review
import com.dugaza.letsdrive.repository.common.Checks
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.support.PageableExecutionUtils
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties


// TODO: deletedAt 처리
// TODO: pageable 테스트해보기,
//   sort 조건 다양하게 넣어보고 제대로 값 나오는지 확인 필요
class ReviewCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : ReviewCustomRepository {
    override fun findAllWithPage(
        targetId: UUID?,
        pageable: Pageable,
    ): Page<Review> {
        Checks.argsIsNotNull(targetId)

        val review = QReview.review
        val count = targetId?.let {
            getCount(targetId)
        } ?: 0L
        val direction = pageable.sort.filter {
            it.property == "createdAt"
        }.first()
            .direction
        val reviewList = jpaQueryFactory
            .selectFrom(review)
            .orderBy(*getOrderSpecifiers(pageable.sort).toTypedArray()) // !
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        return PageableExecutionUtils.getPage(reviewList, pageable) { count }
    }

    private fun getCount(
        targetId: UUID,
    ): Long {
        val review = QReview.review
        return jpaQueryFactory
            .select(review.count())
            .from(review)
            .where(
                review.targetId.eq(targetId)
            )
            .fetchOne() ?: 0L
    }

    private fun getOrderSpecifiers(sort: Sort): List<OrderSpecifier<*>> {
        return sort.map { order ->
            val direction = if (order.isAscending) Order.ASC else Order.DESC
            val propertyNames = getAllPropertyNames(Review::class)
            propertyNames.contains(order.property).takeIf { it }?.let {
                OrderSpecifier(
                    direction,
                    Expressions.path(String::class.java, QReview.review, order.property)
                )
            }
        }.filterNotNull().toList()
    }

    private fun getAllPropertyNames(clazz: KClass<*>): List<String> {
        return clazz.memberProperties.map { it.name }
    }
}