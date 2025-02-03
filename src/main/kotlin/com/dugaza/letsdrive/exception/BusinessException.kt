package com.dugaza.letsdrive.exception

import com.dugaza.letsdrive.converter.MessageConverter

class BusinessException(
    val errorCode: ErrorCode,
    args: Array<Any?>? = null,
    messageConverter: MessageConverter = MessageConverter(),
) : RuntimeException(messageConverter.getMessage(errorCode.code, args))
