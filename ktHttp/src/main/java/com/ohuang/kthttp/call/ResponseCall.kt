package com.ohuang.kthttp.call

import com.ohuang.kthttp.KtHttpConfig
import okhttp3.Call
import okhttp3.Response

private const val key_onResponse = "ResponseShow"

/**
 *  获取Response
 */
fun KtHttpConfig.onResponse(block: ResponseShow) {
    setConfig(key_onResponse, block)
}

fun interface ResponseShow {
    fun onResponse(response: Response)
}

internal fun ResponseCall.onResponse(response: Response) {
    val responseLog = getConfigs()[key_onResponse]
    if (responseLog is ResponseShow) {
        responseLog.onResponse(response)
    }
}

private const val key_responseHook = "ResponseHook"

/**
 *  可修改Response
 */
fun KtHttpConfig.hookResponse(block: ResponseHook) {
    setConfig(key_responseHook, block)
}

fun interface ResponseHook {
    fun hook(response: Response): Response
}

internal fun ResponseCall.hookResponse(response: Response): Response {
    val responseLog = getConfigs()[key_responseHook]
    if (responseLog is ResponseHook) {
        return responseLog.hook(response)
    }
    return response
}

private const val key_Error = "ResponseCallError"

/**
 * 出现异常回调
 */
fun KtHttpConfig.onError(block: ResponseCallError) {
    setConfig(key_Error, block)
}

fun interface ResponseCallError {
    fun onError(throwable: Throwable)
}

private fun ResponseCall.onError(throwable: Throwable){
    val responseLog = getConfigs()[key_Error]
    if (responseLog is ResponseCallError) {
        responseLog.onError(throwable)
    }
}



class ResponseCall(private var call: Call, private val configs: Map<String, Any>) :
    HttpCall<Response> {
    override fun request(error: (Throwable) -> Unit, callback: (Response) -> Unit) {
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                onError(e)
                error(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                try {
                    onResponse(hookResponse(response))
                    callback(response)
                }catch (e:Throwable){
                    onError(e)
                    error(e)
                }
            }
        })
    }

    override fun cancel() {
        call.cancel()
    }

    override fun isCancelled(): Boolean {
        return call.isCanceled()
    }

    override fun isExecuted(): Boolean {
        return call.isExecuted()
    }

    override fun getConfigs(): Map<String, Any> {
        return configs
    }
}