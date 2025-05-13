package com.ohuang.kthttp

import okhttp3.Request

open class HttpRequest(internal var builder :Request.Builder) {

    var url: String = ""
        set(value) {
            field = value
            builder.url(value)
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

    internal fun build(): Request {
        return builder.build()
    }

}

