package com.ohuang.kthttp.call

import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody


open class KtHttpException(msg: String, cause: Throwable? = null) : RuntimeException(msg, cause)

/**
 * HTTP 错误响应异常，携带完整的响应信息
 *
 * 注意：调用 [errorBodyString] 会消费并关闭 response body，
 * 之后再次调用返回缓存值，但 [raw] 返回的 Response 可能已关闭。
 */
open class ErrorResponseException(msg: String, val errorResponse: ErrorResponse) :
    KtHttpException(msg) {

    fun code(): Int = errorResponse.code()
    fun httpMessage(): String = errorResponse.message()
    fun headers(): Headers = errorResponse.headers()
    fun url(): String = errorResponse.url()
    fun errorBodyString(): String = errorResponse.errorBodyString()

    fun close() {
        errorResponse.close()
    }
}


class EmptyBodyException(msg: String, errorResponse: ErrorResponse) : ErrorResponseException(msg, errorResponse)

/**
 * 错误响应
 */
class ErrorResponse(
    private val response: Response,
    val errorBody: ResponseBody?
) {
    fun raw(): Response = response

    fun url(): String = response.request.url.toString()

    fun code(): Int = response.code

    fun message(): String = response.message

    fun headers(): Headers = response.headers

    @Volatile
    private var errorBodyStringCache: String? = null

    /**
     * 错误响应体内容。首次调用会消费 body 并缓存，后续返回缓存值。
     */
    fun errorBodyString(): String {
        val cached = errorBodyStringCache
        if (cached != null) return cached
        try {
            val body = errorBody?.string() ?: ""
            response.close()
            errorBodyStringCache = body
            return body
        } catch (_: Throwable) {
        }
        return ""
    }

    fun close() {
        response.close()
    }

    override fun toString(): String {
        return response.toString()
    }
}