package com.ohuang.kthttp.call

import com.ohuang.kthttp.KtHttpConfig
import okhttp3.Call
import okhttp3.Response

private const val key_responseShow = "ResponseShow"

/**
 *  获取Response
 */
fun KtHttpConfig.showResponse(block: ResponseShow) {
    setConfig(key_responseShow, block)
}

fun interface ResponseShow {
    fun show(response: Response)
}

private fun ResponseCall.showResponse(response: Response) {
    val responseLog = getConfigs()[key_responseShow]
    if (responseLog is ResponseShow) {
        responseLog.show(response)
    }
}

private const val key_responseHook = "ResponseHook"
fun KtHttpConfig.hookResponse(block: ResponseHook) {
    setConfig(key_responseHook, block)
}

fun interface ResponseHook {
    fun hook(response: Response): Response
}

private fun ResponseCall.hookResponse(response: Response): Response {
    val responseLog = getConfigs()[key_responseHook]
    if (responseLog is ResponseHook) {
        return responseLog.hook(response)
    }
    return response
}

class ResponseCall(private var call: Call, private val configs: Map<String, Any>) :
    HttpCall<Response> {
    override fun request(error: (Throwable) -> Unit, callback: (Response) -> Unit) {
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                error(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                showResponse(hookResponse(response))
                callback(response)
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