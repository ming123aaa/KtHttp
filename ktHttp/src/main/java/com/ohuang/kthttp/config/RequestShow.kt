package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.ResponseCall
import com.ohuang.kthttp.getConfigForType
import okhttp3.Call

private const val key_RequestShow = "RequestShow"

fun interface RequestShow {
    fun onRequestShow(call: Call)
}

/**
 *  开始请求时回调
 *   @param isOverride 是否覆盖原有的回调
 */
fun KtHttpConfig.onRequest(isOverride: Boolean = false, block: (Call) -> Unit) {
    var requestShow: RequestShow = object : RequestShow {
        override fun onRequestShow(call: Call) {
            block(call)
        }
    }
    if (!isOverride) {
        var configForType = getConfigForType<RequestShow>(key_RequestShow)
        if (configForType != null) {
            requestShow = object : RequestShow {
                override fun onRequestShow(call: Call) {
                    configForType.onRequestShow(call)
                    block(call)
                }
            }
        }
    }
    setConfig(key_RequestShow, requestShow)
}


internal fun ResponseCall.callRequestShow(call: Call) {
    val responseLog = getConfigs()[key_RequestShow]
    if (responseLog is RequestShow) {
        responseLog.onRequestShow(call)
    }
}
