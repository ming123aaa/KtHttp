package com.ohuang.kthttp.call

import com.ohuang.kthttp.config.callRequestShow
import com.ohuang.kthttp.config.callResponse
import com.ohuang.kthttp.config.hookResponse
import com.ohuang.kthttp.config.onError
import okhttp3.Call
import okhttp3.Response


class ResponseCall(private var call: Call, private val configs: MutableMap<String, Any>) :
    HttpCall<Response> {
    override fun request(error: (Throwable) -> Unit, callback: (Response) -> Unit) {
        callRequestShow(call)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                onError(e, call, null)
                error(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                var hookResponse: okhttp3.Response = response
                try {
                    hookResponse = hookResponse(response)
                    callResponse(hookResponse)
                    callback(hookResponse)
                } catch (e: Throwable) {
                    onError(e, call, hookResponse)
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


class ResponseBuilderCall(private var builder: () -> ResponseCall) :
    HttpCall<Response> {
    private var responseCall: ResponseCall? = null
    @Volatile private var isCreated: Boolean = false
    private var mThrowable: Throwable? = null

    override fun request(error: (Throwable) -> Unit, callback: (Response) -> Unit) {
        checkResponseCall()
        val call = responseCall
        if (call == null) {
            error(mThrowable ?: KtHttpException("ResponseCall build error"))
        } else {
            call.request(error, callback)
        }
    }

    @Synchronized
    private fun checkResponseCall() {
        if (!isCreated) {
            isCreated = true
            try {
                responseCall = builder()
            } catch (e: Throwable) {
                mThrowable = e
            }
        }
    }

    private fun getResponseCallOrThrow(): ResponseCall {
        checkResponseCall()
        val call = responseCall
        return call ?: throw (mThrowable ?: KtHttpException("ResponseCall build error"))
    }

    override fun getOkhttpCall(): Call {
        return getResponseCallOrThrow().getOkhttpCall()
    }

    override fun cancel() {
        responseCall?.cancel()
    }

    override fun isCancelled(): Boolean {
        return responseCall?.isCancelled() ?: false
    }

    override fun isExecuted(): Boolean {
        return responseCall?.isExecuted() ?: false
    }

    override fun getConfigs(): MutableMap<String, Any> {
        return getResponseCallOrThrow().getConfigs()
    }
}