package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.ResponseCall
import okhttp3.Call

private const val key_RequestShow = "RequestShow"
fun interface RequestShow {
    fun onRequestShow(call: Call)
}
/**
 *  开始请求时回调
 */
fun KtHttpConfig.onRequest(block: (Call)-> Unit) {
    setConfig(key_RequestShow, object : RequestShow {
        override fun onRequestShow(call: Call) {
            block(call)
        }
    })
}


internal fun ResponseCall.callRequestShow(call: Call) {
    val responseLog = getConfigs()[key_RequestShow]
    if (responseLog is RequestShow) {
        responseLog.onRequestShow(call)
    }
}
