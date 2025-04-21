package com.ohuang.kthttp.call

import com.ohuang.kthttp.KtHttpConfig
import okhttp3.Call
import okhttp3.Response

private const val key_responseLog = "ResponseLog"
fun KtHttpConfig.logResponse(block: ResponseLog) {
    setConfigs(key_responseLog, block)
}


fun interface ResponseLog {
    fun log(response: Response)
}

private fun ResponseCall.logResponse(response: Response) {
    val responseLog = getConfigs().get(key_responseLog)
    if (responseLog is ResponseLog) {
        responseLog.log(response)
    }
}

class ResponseCall(private var call: Call, private val configs: Map<String, Any>) :
    HttpCall<Response> {
    override fun request(error: (Throwable) -> Unit, callback: (Response) -> Unit) {
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                error(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                logResponse(response)
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