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
fun HttpRequest.urlParams(url: String, block:  RequestParams.() -> Unit = {}) {
    val requestParams = RequestParams()
    block.invoke(requestParams)
    urlParams(url, requestParams.map)
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

class RequestParams(val map: MutableMap<String, String> = TreeMap<String, String>()) {
    fun addParam(key: String, value: String) {
        map[key] = value
    }

    fun removeParam(key: String) {
        map.remove(key)
    }
}

private fun getFormBody(params: Map<String, String>): RequestBody {
    val builder = FormBody.Builder()
    params.entries.forEach {
        builder.add(it.key, it.value)
    }
    return builder.build()
}