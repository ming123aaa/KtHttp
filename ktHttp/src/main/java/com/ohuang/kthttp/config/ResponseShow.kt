package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.ResponseCall
import okhttp3.Response

private const val key_onResponse = "ResponseShow"

fun interface ResponseShow {
    fun onResponse(response: Response)
}
/**
 *  获取Response
 */
fun KtHttpConfig.onResponse(block: ResponseShow) {
    setConfig(key_onResponse, block)
}


internal fun ResponseCall.callResponse(response: Response) {
    val responseLog = getConfigs()[key_onResponse]
    if (responseLog is ResponseShow) {
        responseLog.onResponse(response)
    }
}
