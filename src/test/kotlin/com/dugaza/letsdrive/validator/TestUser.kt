package com.dugaza.letsdrive.validator

data class TestUser(
    @field:ValidEnum(
        enumClass = TestStatus::class,
        message = "Status must be ACTIVE, INACTIVE, or PENDING",
        // default value
        allowNull = false,
        // default value
        ignoreCase = false,
    )
    val status: String?,
)

data class TestUserAllowNull(
    @field:ValidEnum(
        enumClass = TestStatus::class,
        message = "Status must be ACTIVE, INACTIVE, or PENDING",
        allowNull = true,
        // default value
        ignoreCase = false,
    )
    val status: String?,
)

data class TestUserIgnoreCase(
    @field:ValidEnum(
        enumClass = TestStatus::class,
        message = "Status must be ACTIVE, INACTIVE, or PENDING",
        // default value
        allowNull = false,
        ignoreCase = true,
    )
    val status: String?,
)
