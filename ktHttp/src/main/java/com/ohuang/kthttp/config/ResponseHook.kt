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
 *
 *  重复调用会覆盖
 */
fun KtHttpConfig.hookResponse(block: (Response)-> Response) {
    setConfig(key_responseHook, object :ResponseHook{
        override fun hook(response: Response): Response {
            return block(response)
        }
    })
}



internal fun ResponseCall.hookResponse(response: Response): Response {
    val responseLog = getConfigs()[key_responseHook]
    if (responseLog is ResponseHook) {
        return responseLog.hook(response)
    }
    return response
}
