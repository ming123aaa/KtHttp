package com.ohuang.kthttp.call

import com.ohuang.kthttp.config.callResponse
import com.ohuang.kthttp.config.hookResponse
import com.ohuang.kthttp.config.onError
import okhttp3.Call
import okhttp3.Response


class ResponseCall(private var call: Call, private val configs: MutableMap<String, Any>) :
    HttpCall<Response> {
    override fun request(error: (Throwable) -> Unit, callback: (Response) -> Unit) {
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                onError(e,call, null)
                error(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {

                try {
                    callResponse(hookResponse(response))
                    callback(response)
                } catch (e: Throwable) {
                    onError(e,call,response)
                    error(e)
                }
            }
        })
    }

    override fun getOkhttpCall(): Call {
        return call
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

    override fun getConfigs(): MutableMap<String, Any> {
        return configs
    }
}