package com.dugaza.letsdrive.exception

class BusinessException(
    val errorCode: ErrorCode,
) : RuntimeException()
