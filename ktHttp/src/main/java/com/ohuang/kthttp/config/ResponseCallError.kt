package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.ResponseCall
import okhttp3.Call
import okhttp3.Response

private const val key_Error = "ResponseCallError"
fun interface ResponseCallError {
    fun onError(throwable: Throwable, call: Call,response: Response?)
}

/**
 * 出现异常回调
 */
fun KtHttpConfig.onError(block: (throwable: Throwable, call: Call,response: Response?)-> Unit) {
    setConfig(key_Error, object :  ResponseCallError {
        override fun onError(
            throwable: Throwable,
            call: Call,
            response: Response?
        ) {
            block(throwable,call,response)
        }
    })
}
/**
 * 出现异常回调
 */
fun KtHttpConfig.onError(block: (Throwable) -> Unit) {
    setConfig(key_Error, object : ResponseCallError {
        override fun onError(
            throwable: Throwable,
            call: Call,
            response: Response?
        ) {
            block(throwable)
        }
    })
}



internal fun ResponseCall.onError(throwable: Throwable, call: Call,response: Response?) {
    val responseLog = getConfigs()[key_Error]
    if (responseLog is ResponseCallError) {
        responseLog.onError(throwable,call, response)
    }
}