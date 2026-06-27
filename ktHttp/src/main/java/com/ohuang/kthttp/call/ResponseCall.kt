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
                var hookResponse = response
                try {
                    var hookResponse = hookResponse(response)
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
    private var isCreated: Boolean = false
    private var mThrowable: Throwable? = null

    override fun request(error: (Throwable) -> Unit, callback: (Response) -> Unit) {
        checkResponseCall()
        if (responseCall==null){
            error(mThrowable?: KtHttpException("ResponseCall build error"))
        }else{
            responseCall!!.request(error,callback)
        }


    }

    private  fun checkResponseCall() {

        if (!isCreated) {
            isCreated = true
            try {
                responseCall = builder()
            } catch (e: Throwable) {
                mThrowable = e
            }
        }

    }

    private fun throwBuilderError(){
        if (isCreated&&responseCall==null){
            throw mThrowable?: KtHttpException("ResponseCall build error")
        }
    }

    override fun getOkhttpCall(): Call {
        checkResponseCall()
        throwBuilderError()
        return responseCall!!.getOkhttpCall()
    }

    override fun cancel() {
        checkResponseCall()
        throwBuilderError()
        responseCall?.cancel()
    }

    override fun isCancelled(): Boolean {
        checkResponseCall()
        throwBuilderError()
        return responseCall?.isCancelled() ?: false
    }

    override fun isExecuted(): Boolean {
        checkResponseCall()
        throwBuilderError()
        return responseCall?.isExecuted() ?: false
    }

    override fun getConfigs(): MutableMap<String, Any> {
        checkResponseCall()
        throwBuilderError()
        return responseCall!!.getConfigs()
    }
}