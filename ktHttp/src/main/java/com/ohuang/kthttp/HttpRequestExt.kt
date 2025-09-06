package com.ohuang.kthttp

import com.ohuang.kthttp.transform.ktHttp_mGson
import com.ohuang.kthttp.util.UrlAddParams
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.TreeMap

/**
 * 设置url,可新增url参数
 *
 * 若需要查看或删除url中存在的参数请使用[urlParamsEdit]方法
 */
fun HttpRequest.urlParams(url: String, params: Map<String, String>) {
    url(UrlAddParams.urlAddParams(url, params))
}

/**
 * 设置url,可新增url参数
 *
 * 若需要查看或删除url中存在的参数请使用[urlParamsEdit]方法
 */
fun HttpRequest.urlParams(url: String, block: RequestParams.() -> Unit = {}) {
    val requestParams = RequestParams()
    block.invoke(requestParams)
    urlParams(url, requestParams.map)
}

/**
 * 设置url,可编辑url的参数
 *
 * 与[urlParams]相比,[urlParamsEdit]方法可以查看和删除在url已存在的参数
 */
fun HttpRequest.urlParamsEdit(url: String, block: RequestParams.() -> Unit = {}){
    var urlParams = UrlAddParams.getUrlParams(url)
    val requestParams = RequestParams(urlParams)
    block.invoke(requestParams)
    url(UrlAddParams.urlReplaceParams(url, requestParams.map))
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

/**
 * 添加headers
 */
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
 *
 */
fun HttpRequest.post(params: Map<String, String>) {
    builder.post(getFormBody(params))
}

/**
 *  post请求
 *  提交的是json数据类型
 */
fun HttpRequest.postJson(json: String) {
    builder.post(body = json.toRequestBody("application/json; charset=utf-8".toMediaType()))
}
/**
 *  post请求
 *  提交的是json数据类型
 */
fun HttpRequest.postJsonForAny(obj: Any) {
    postJson(json = ktHttp_mGson.toJson(obj))
}


/**
 * post请求
 *  提交的是json数据类型
 */
fun HttpRequest.postJson(params: Map<String, String>) {
    postJson(json = ktHttp_mGson.toJson(params))
}
/**
 * post请求
 *  提交的是json数据类型
 */
fun HttpRequest.postJson(block: RequestParams.() -> Unit = {}) {
    val requestParams = RequestParams()
    block.invoke(requestParams)
    postJson(params = requestParams.map)
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

    fun hasParam(key: String): Boolean{
        return map.containsKey(key)
    }

    fun getParam(key: String): String?{
        return map[key]
    }

    fun getAllParams(): Map<String, String> {
        return map
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