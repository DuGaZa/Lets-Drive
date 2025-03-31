package com.dugaza.letsdrive.repository.common

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.SimpleExpression

object Checks {
    fun argsIsNotNull(vararg args: Any?) {
        require(
            !args.all {
                it == null
            },
        ) {
            "적어도 한개 이상의 인자가 제공되어야 합니다."
        }
    }

    fun <T> SimpleExpression<T>.eqIfNotNull(value: T?): BooleanExpression? {
        return if (value != null) this.eq(value) else null
    }
}
