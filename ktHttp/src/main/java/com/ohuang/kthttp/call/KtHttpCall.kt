package com.ohuang.kthttp.call



abstract class  KtHttpCall<T,B>(protected val call: HttpCall<B>):HttpCall<T> {
    
    override fun cancel() {
        call.cancel()
    }

    override fun isCancelled(): Boolean {
        return call.isCancelled()
    }

    override fun isExecuted(): Boolean {
        return call.isExecuted()
    }

    override fun getConfigs(): Map<String, Any> {
        return call.getConfigs()
    }
}