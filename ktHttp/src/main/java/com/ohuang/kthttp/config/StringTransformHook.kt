package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.HttpCall
import okhttp3.Response

private const val key_hookStringBody = "StringTransformHook"

fun interface StringTransformHook {
    fun hook(response: Response): String
}

/**
 * 拦截Body字符串，返回新的字符串
 * 重复调用[hookStringBody],[hookStringResponse]会覆盖
 *
 */
fun KtHttpConfig.hookStringBody(block: (String) -> String) {
    hookStringResponse(block={response->
        block(response.body!!.string())
    })
}

/**
 * 拦截Body字符串，返回新的字符串
 * 重复调用[hookStringBody],[hookStringResponse]会覆盖
 */
fun KtHttpConfig.hookStringResponse(block: (Response) -> String) {
    setConfig(key_hookStringBody, object : StringTransformHook {
        override fun hook(response: Response): String {
            return block(response)
        }
    })
}

internal fun <T> HttpCall<T>.hookStringBody(response: Response): String {
    val logString = getConfigs()[key_hookStringBody]
    if (logString is StringTransformHook) {
        return logString.hook(response)
    }
    return response.body!!.string()
}