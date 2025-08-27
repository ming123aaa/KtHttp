package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.ResponseCall
import com.ohuang.kthttp.getConfigForType
import okhttp3.Call
import okhttp3.Response

private const val key_Error = "ResponseCallError"

fun interface ResponseCallError {
    fun onError(throwable: Throwable, call: Call, response: Response?)
}

/**
 *
 * 出现异常回调
 * @param isOverride 是否覆盖原有的回调
 */
fun KtHttpConfig.onError(
    isOverride: Boolean = false,
    block: (throwable: Throwable, call: Call, response: Response?) -> Unit
) {
    var responseCallError:ResponseCallError=object : ResponseCallError {
        override fun onError(
            throwable: Throwable,
            call: Call,
            response: Response?
        ) {
            block(throwable, call, response)
        }
    }
    if (!isOverride){
        var configForType = getConfigForType<ResponseCallError>(key_Error)
        if (configForType != null){
            responseCallError=object : ResponseCallError {
                override fun onError(
                    throwable: Throwable,
                    call: Call,
                    response: Response?
                ) {
                    configForType.onError(throwable, call, response)
                    block(throwable, call, response)
                }
            }
        }
    }
    setConfig(key_Error,responseCallError)
}

/**
 * 出现异常回调
 *  @param isOverride 是否覆盖原有的回调
 */
fun KtHttpConfig.onError(isOverride: Boolean = false, block: (Throwable) -> Unit) {
    onError(
        isOverride = isOverride,
        block = { throwable: Throwable, call: Call, response: Response? ->
            block(throwable)
        })
}


internal fun ResponseCall.onError(throwable: Throwable, call: Call, response: Response?) {
    val responseLog = getConfigs()[key_Error]
    if (responseLog is ResponseCallError) {
        responseLog.onError(throwable, call, response)
    }
}