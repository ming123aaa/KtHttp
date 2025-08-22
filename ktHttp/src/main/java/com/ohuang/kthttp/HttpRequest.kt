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

    fun hookRequestBuild(block: (Request) -> Request) {
        requestBuildHook = block
    }

    fun onRequestBuild(block: (Request) -> Unit) {
        requestBuildCall = block
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

