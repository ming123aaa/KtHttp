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
fun KtHttpConfig.onResponse(block: (Response)-> Unit) {
    setConfig(key_onResponse, object : ResponseShow {
        override fun onResponse(response: Response) {
            block(response)
        }
    })
}


internal fun ResponseCall.callResponse(response: Response) {
    val responseLog = getConfigs()[key_onResponse]
    if (responseLog is ResponseShow) {
        responseLog.onResponse(response)
    }
}
