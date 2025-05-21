package com.ohuang.kthttp.call

import okhttp3.Call


interface HttpCall<T> {
    fun request(error: (Throwable) -> Unit = {}, callback: (T) -> Unit)

    fun getOkhttpCall(): Call

    fun cancel()

    fun isCancelled(): Boolean

    fun isExecuted(): Boolean

    fun getConfigs(): MutableMap<String, Any>
}

