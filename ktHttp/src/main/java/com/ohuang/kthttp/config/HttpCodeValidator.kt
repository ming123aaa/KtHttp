package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig

private const val key_validHttpCode = "HttpCodeValidator"

fun interface HttpCodeValidator {
    fun isValid(code: Int): Boolean
}

/**
 * 自定义有效的 HTTP 状态码判断逻辑，默认 200-299 为成功
 * 传入 null 表示所有状态码都不抛异常
 */
fun KtHttpConfig.validHttpCode(validator: ((Int) -> Boolean)?) {
    if (validator == null) {
        setConfig(key_validHttpCode, ValidHttpCodeNone)
    } else {
        setConfig(key_validHttpCode, HttpCodeValidator { code -> validator(code) })
    }
}

internal object ValidHttpCodeNone

internal fun resolveValidHttpCode(configs: MutableMap<String, Any>, code: Int): Boolean {
    return when (val configured = configs[key_validHttpCode]) {
        is HttpCodeValidator -> configured.isValid(code)
        ValidHttpCodeNone -> true
        else -> code in 200..299
    }
}