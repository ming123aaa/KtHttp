package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.ResponseCall
import com.ohuang.kthttp.getConfigForType
import okhttp3.Response

private const val key_onResponse = "ResponseShow"

fun interface ResponseShow {
    fun onResponse(response: Response)
}
/**
 *  获取Response
 *   @param isOverride 是否覆盖原有的回调
 */
fun KtHttpConfig.onResponse(isOverride: Boolean = false,block: (Response)-> Unit) {
    var responseShow: ResponseShow=object : ResponseShow {
        override fun onResponse(response: Response) {
            block(response)
        }
    }
    if (!isOverride){
        var configForType = getConfigForType<ResponseShow>(key_onResponse)
        if (configForType!=null){
            responseShow = object : ResponseShow {
                override fun onResponse(response: Response) {
                    configForType.onResponse(response)
                    block(response)
                }
            }
        }
    }

    setConfig(key_onResponse,responseShow )
}


internal fun ResponseCall.callResponse(response: Response) {
    val responseLog = getConfigs()[key_onResponse]
    if (responseLog is ResponseShow) {
        responseLog.onResponse(response)
    }
}
