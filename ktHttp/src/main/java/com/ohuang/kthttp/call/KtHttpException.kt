package com.ohuang.kthttp.call

import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody


open class KtHttpException(msg: String) : RuntimeException(msg)
open class ErrorResponseException(msg: String, val errorResponse: ErrorResponse) :
    KtHttpException(msg) {

    fun code(): Int {
        return errorResponse.code()
    }

    fun httpMessage(): String {
        return errorResponse.message()
    }

    fun errorBodyString(): String {
        return errorResponse.errorBodyString()
    }
}

class CodeNot200Exception(msg: String, errorResponse: ErrorResponse) : ErrorResponseException(
    msg,
    errorResponse
)

class EmptyBodyException(msg: String, errorResponse: ErrorResponse) : ErrorResponseException(
    msg,
    errorResponse
)

/**
 * 错误响应
 */
class ErrorResponse(
    private val response: Response,
    val errorBody: ResponseBody?
) {
    /**
     * 原始响应
     */
    fun raw(): Response {
        return response
    }

    fun url(): String {
        return response.request.url.toString()
    }

    fun code(): Int {
        return response.code
    }

    fun message(): String {
        return response.message
    }

    fun headers(): Headers {
        return response.headers
    }

    private var errorBodyString: String? = null

    /**
     * 错误响应体内容
     */
    fun errorBodyString(): String {
        if (errorBodyString != null) {
            return errorBodyString!!
        }
        try {
              errorBodyString = errorBody?.string() ?: ""
        }catch(e: Throwable) {

        }
        return errorBodyString ?: ""
    
    }

    override fun toString(): String {
        return response.toString()
    }

}