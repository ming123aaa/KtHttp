package com.ohuang.kthttp.call

import okhttp3.Call


abstract class  KtHttpCall<T,B>(protected val call: HttpCall<B>):HttpCall<T> {
    
    override fun cancel() {
        call.cancel()
    }

    override fun getOkhttpCall(): Call {
        return call.getOkhttpCall()
    }

    override fun isCancelled(): Boolean {
        return call.isCancelled()
    }

    override fun isExecuted(): Boolean {
        return call.isExecuted()
    }

    override fun getConfigs(): MutableMap<String, Any> {
        return call.getConfigs()
    }
}