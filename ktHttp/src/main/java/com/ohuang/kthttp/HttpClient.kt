package com.ohuang.kthttp

import com.ohuang.kthttp.call.HttpCall

import com.ohuang.kthttp.call.toHttpCall
import com.ohuang.kthttp.call.toStringHttpCall
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response

class HttpClient(var okHttpClient: OkHttpClient = OkHttpClient()) {

    /**
     * 返回okhttp的Call 不封装
     * 网络请求，获取响应内容
     * @param block 请求参数
     */
    fun newCall(block: HttpRequest.() -> Unit): Call {
        val httpRequest = HttpRequest()
        block.invoke(httpRequest)
        return okHttpClient.newCall(httpRequest.build())
    }

    /**
     * 网络请求，获取响应内容
     **/
    fun responseCall(block: HttpRequest.() -> Unit): HttpCall<Response> {
        return ResponseCall(newCall(block))
    }

    /**
     * 网络请求，获取字符串内容
     *
     * @param block 请求参数
     */
    fun stringCall(block: HttpRequest.() -> Unit): HttpCall<String> {
        return responseCall(block).toStringHttpCall()
    }

    /**
     * 网络请求，获取对象内容
     * @param transform 类型转换器
     * @param block 请求参数
     */
    fun <T> httpCall(transform: Transform<T>, block: HttpRequest.() -> Unit): HttpCall<T> {
        return responseCall(block).toHttpCall(transform)
    }
}

class ResponseCall(private var call: Call) : HttpCall<Response> {
    override fun request(error: (Throwable) -> Unit, callback: (Response) -> Unit) {
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                error(e)
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                callback(response)
            }
        })
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
}






