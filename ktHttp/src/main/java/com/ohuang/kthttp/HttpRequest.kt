package com.ohuang.kthttp

import okhttp3.Request

open class HttpRequest(internal var builder: Request.Builder) {

    var url: String = ""
        set(value) {
            field = value
            builder.url(value)
        }

    fun requestBuilder(block: (Request.Builder) -> Unit) {
        block.invoke(builder)
    }

    /**
     * 没提供的方法可以加
     * okhttp的Request.Builder
     */
    fun requestBuilderBlock(block: Request.Builder.() -> Unit) {
        block.invoke(builder)
    }

    /**
     * 请求的url
     * 想要加参数可以用 urlParams()
     * @param url
     */
    fun url(url: String) {
        this.url = url
    }


    fun addHeader(key: String, value: String) {
        builder.addHeader(key, value)
    }

    private var requestBuildHook: ((Request) -> Request)? = null
    private var requestBuildCall: ((Request) -> Unit)? = null

    /**
     * 可修改Request
     * 重复调用会覆盖
     */
    fun hookRequestBuild(block: (Request) -> Request) {
        requestBuildHook = block
    }

    /**
     *  @param isOverride 是否覆盖原有的回调
     */
    fun onRequestBuild(isOverride: Boolean = false, block: (Request) -> Unit) {
        var newCall= block
        if (!isOverride) {
            var oldCall = requestBuildCall
            if (oldCall != null) {
                newCall = { request ->
                    oldCall.invoke(request)
                    block.invoke(request)
                }
            }
        }
        requestBuildCall = newCall
    }

    internal fun build(): Request {
        var build = builder.build()
        if (requestBuildHook != null) {
            build = requestBuildHook!!.invoke(build)
        }
        requestBuildCall?.invoke(build)
        return build
    }

}

