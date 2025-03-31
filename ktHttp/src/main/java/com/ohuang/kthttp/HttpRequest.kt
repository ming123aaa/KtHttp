package com.ohuang.kthttp

import okhttp3.Call
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.TreeMap

class HttpRequest() {
    internal var builder = Request.Builder().get()
    private var url: String = ""

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
        if (url.isEmpty()) {
            throw IllegalArgumentException("url is empty")
        }
        return builder.url(url).build()
    }

}

