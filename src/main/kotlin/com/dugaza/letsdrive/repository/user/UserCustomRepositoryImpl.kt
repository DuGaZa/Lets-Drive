package com.dugaza.letsdrive.repository.user

import com.dugaza.letsdrive.entity.user.QUser
import com.dugaza.letsdrive.entity.user.User
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.UUID

class UserCustomRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : UserCustomRepository {
    override fun findUserByUserId(userId: UUID): User? {
        val user = QUser.user
        return jpaQueryFactory.selectFrom(user)
            .where(user.id!!.eq(userId))
            .fetchOne()
    }
}
