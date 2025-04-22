package com.ohuang.kthttp

import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.toStringHttpCall
import com.ohuang.kthttp.call.toStringHttpCallSafe
import com.ohuang.kthttp.call.toTransformCall
import com.ohuang.kthttp.transform.Transform


/**
 * 网络请求，获取字符串内容
 * code==200 才会正确返回结果
 * @param block 请求参数
 */
fun HttpClient.stringCallSafe(block: HttpRequest.() -> Unit): HttpCall<String> {
    return responseCall(block).toStringHttpCallSafe()
}


/**
 * 网络请求，获取对象内容
 * @param transform 类型转换器
 * @param block 请求参数
 */
fun <T> HttpClient.httpCallNotCheck(transform: Transform<T>, block: HttpRequest.() -> Unit): HttpCall<T> {
    return responseCall(block).toTransformCall(transform)
}