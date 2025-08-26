package com.ohuang.kthttp

import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.ResponseCall
import com.ohuang.kthttp.call.toStringHttpCall
import com.ohuang.kthttp.call.toTransformCall
import com.ohuang.kthttp.transform.ResponseConvert
import com.ohuang.kthttp.transform.Transform
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 *
 * @param okHttpClient okhttp客户端
 * @param globalKtConfigCall 全局配置，会被请求配置和强制配置给覆盖
 * @param forceKtConfigCall 强制配置, 会覆盖全局配置和请求配置
 */
class HttpClient(
    var okHttpClient: OkHttpClient = OkHttpClient(),
    private var globalKtConfigCall: KtHttpRequest.() -> Unit = {},
    private var forceKtConfigCall: KtHttpRequest.() -> Unit = {}
) {

    /**
     * 返回okhttp的Call 不封装
     * 网络请求，获取响应内容
     * @param block 请求参数
     */
    @Deprecated(message = "推荐使用responseCall()")
    fun newCall(block: HttpRequest.() -> Unit): Call {
        val httpRequest = createHttpRequest(block)
        return createCall(httpRequest)
    }

    private fun createHttpRequest(block: KtHttpRequest.() -> Unit): KtHttpRequest {
        val httpRequest = KtHttpRequest(
            KtHttpConfigImpl(), Request.Builder().get()
        )
        globalKtConfigCall.invoke(httpRequest)
        block.invoke(httpRequest)
        forceKtConfigCall.invoke(httpRequest)
        return httpRequest
    }

    private fun createCall(httpRequest: HttpRequest): Call {
        return okHttpClient.newCall(httpRequest.build())
    }

    /**
     * 网络请求，获取响应内容
     **/
    fun responseCall(block: KtHttpRequest.() -> Unit): HttpCall<Response> {
        val httpRequest = createHttpRequest(block)
        return ResponseCall(createCall(httpRequest), httpRequest.configs)
    }

    /**
     * 网络请求，获取字符串内容
     * @param block 请求参数
     */
    fun stringCall(block: KtHttpRequest.() -> Unit): HttpCall<String> {
        return responseCall(block).toStringHttpCall()
    }


    /**
     * 网络请求，获取对象内容
     * @param transform 类型转换器
     * @param block 请求参数
     */
    fun <T> httpCall(transform: Transform<T>, block: KtHttpRequest.() -> Unit): HttpCall<T> {
        return responseCall(block).toTransformCall(transform)
    }

    /**
     * 网络请求，获取对象内容
     * @param transform 类型转换器
     * @param block 请求参数
     */
    fun <T> httpCall(transform: ResponseConvert<T>, block: KtHttpRequest.() -> Unit): HttpCall<T> {
        return responseCall(block).toTransformCall(transform)
    }


}








