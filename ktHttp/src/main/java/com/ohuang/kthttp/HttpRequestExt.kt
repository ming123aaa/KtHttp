package com.ohuang.kthttp

import com.ohuang.kthttp.util.UrlAddParams
import okhttp3.FormBody
import okhttp3.RequestBody
import java.util.TreeMap

/**
 * 需要加参数的url
 */
fun HttpRequest.urlParams(url: String, params: Map<String, String>) {
    url(UrlAddParams.urlAddParams(url, params))
}

/**
 * 需要加参数的url
 */
fun HttpRequest.urlParams(url: String, block: RequestParams.() -> Unit = {}) {
    val requestParams = RequestParams()
    block.invoke(requestParams)
    urlParams(url, requestParams.map)
}


/**
 * okhttp 请求的builder
 */
fun HttpRequest.okhttpBuilder(block: okhttp3.Request.Builder.() -> Unit) {
    requestBuilderBlock(block)
}

/**
 * 添加headers
 */
fun HttpRequest.addHeaders(headers: Map<String, String>) {
    headers.entries.forEach {
        addHeader(it.key, it.value)
    }
}

fun HttpRequest.addHeaders(block: RequestParams.() -> Unit = {}) {
    val requestParams = RequestParams()
    block.invoke(requestParams)
    addHeaders(requestParams.getParams())
}

/**
 * 默认
 * get请求
 */
fun HttpRequest.get() {
    builder.get()
}

/**
 * post请求
 *
 * @param body
 */
fun HttpRequest.post(body: RequestBody) {
    builder.post(body)
}

/**
 * 需要加参数的post请求
 */
fun HttpRequest.post(params: Map<String, String>) {
    builder.post(getFormBody(params))
}

/**
 * 需要加参数的post请求
 */
fun HttpRequest.post(block: RequestParams.() -> Unit = {}) {
    val requestParams = RequestParams()
    block.invoke(requestParams)
    post(requestParams.map)
}

class RequestParams(internal val map: MutableMap<String, String> = TreeMap<String, String>()) {
    fun addParam(key: String, value: String) {
        map[key] = value
    }
    fun getParams(): Map<String, String> {
        return map
    }

    fun addAllParams(params: Map<String, String>) {
        params.entries.forEach {
            map[it.key] = it.value
        }
    }

    fun removeParam(key: String) {
        map.remove(key)
    }

    fun removeAllParams() {
        map.clear()
    }
}

private fun getFormBody(params: Map<String, String>): RequestBody {
    val builder = FormBody.Builder()
    params.entries.forEach {
        builder.add(it.key, it.value)
    }
    return builder.build()
}