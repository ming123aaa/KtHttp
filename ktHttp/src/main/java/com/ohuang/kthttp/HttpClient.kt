package com.ohuang.kthttp

import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.ResponseCall

import com.ohuang.kthttp.call.toHttpCall
import com.ohuang.kthttp.call.toSafeTransformCall
import com.ohuang.kthttp.call.toStringHttpCall
import com.ohuang.kthttp.call.toTransformCall
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response

class HttpClient(var okHttpClient: OkHttpClient = OkHttpClient()) {

    /**
     * 返回okhttp的Call 不封装
     * 网络请求，获取响应内容
     * @param block 请求参数
     */
    @Deprecated(message="")
    fun newCall(block: HttpRequest.() -> Unit): Call {
        val httpRequest = createHttpRequest(block)
        return createCall(httpRequest)
    }

    private fun createHttpRequest(block: HttpRequest.() -> Unit): HttpRequest{
        val httpRequest = HttpRequest()
        block.invoke(httpRequest)
        return httpRequest
    }

    private fun createCall(httpRequest:HttpRequest): Call{
        return okHttpClient.newCall(httpRequest.build())
    }

    /**
     * 网络请求，获取响应内容
     **/
    fun responseCall(block: HttpRequest.() -> Unit): HttpCall<Response> {
        val httpRequest = createHttpRequest(block)
        return ResponseCall(createCall(httpRequest),httpRequest.configs)
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
        return responseCall(block).toSafeTransformCall(transform)
    }
}








