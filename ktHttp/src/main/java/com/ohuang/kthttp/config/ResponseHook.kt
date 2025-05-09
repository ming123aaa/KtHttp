package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.ResponseCall
import okhttp3.Response


private const val key_responseHook = "ResponseHook"
fun interface ResponseHook {
    fun hook(response: Response): Response
}

/**
 *  可修改Response
 */
fun KtHttpConfig.hookResponse(block: ResponseHook) {
    setConfig(key_responseHook, block)
}



internal fun ResponseCall.hookResponse(response: Response): Response {
    val responseLog = getConfigs()[key_responseHook]
    if (responseLog is ResponseHook) {
        return responseLog.hook(response)
    }
    return response
}
