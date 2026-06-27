package com.ohuang.kthttp

import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.ResponseBuilderCall
import com.ohuang.kthttp.call.ResponseCall
import com.ohuang.kthttp.call.toConvertCall
import com.ohuang.kthttp.call.toStringHttpCall
import com.ohuang.kthttp.call.toTransformCall
import com.ohuang.kthttp.transform.ResponseConvert
import com.ohuang.kthttp.transform.Transform
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

typealias KtHttp = HttpClient

/**
 *
 * @param okHttpClient okhttp客户端
 * @param globalKtConfigCall 全局配置，用于添加默认请求参数,不会覆盖参数。
 * @param forceKtConfigCall 强制配置, 用于添加或覆盖请求参数。
 * @param deferRequestBuild 是否延迟构建请求。true表示在实际执行请求时才构建Request对象（延迟执行，节省资源）；false表示调用时立即构建Request对象。默认为true
 */
class HttpClient(
    var okHttpClient: OkHttpClient = OkHttpClient(),
    private var globalKtConfigCall: KtHttpRequest.() -> Unit = {},
    private var forceKtConfigCall: KtHttpRequest.() -> Unit = {},
    private var deferRequestBuild: Boolean = true
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
        if (deferRequestBuild) {
            return ResponseBuilderCall({
                val httpRequest = createHttpRequest(block)
                return@ResponseBuilderCall ResponseCall(
                    createCall(httpRequest),
                    httpRequest.configs
                )
            })
        }
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
     * @param convert 类型转换器
     * @param block 请求参数
     */
    fun <T> httpCall(convert: ResponseConvert<T>, block: KtHttpRequest.() -> Unit): HttpCall<T> {
        return responseCall(block).toConvertCall(convert)
    }


}








