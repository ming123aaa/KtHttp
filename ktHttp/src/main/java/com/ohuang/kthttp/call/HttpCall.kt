package com.ohuang.kthttp.call




interface HttpCall<T> {
    fun request(error: (Throwable) -> Unit={}, callback: (T) -> Unit)

    fun cancel()

    fun isCancelled(): Boolean

    fun isExecuted():Boolean

    fun getConfigs():Map<String,Any>
}

