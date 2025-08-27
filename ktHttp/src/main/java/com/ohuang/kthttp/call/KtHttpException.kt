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
}

class CodeNot200Exception(msg: String, errorResponse: ErrorResponse) : ErrorResponseException(
    msg,
    errorResponse
)

class EmptyBodyException(msg: String, errorResponse: ErrorResponse) : ErrorResponseException(
    msg,
    errorResponse
)


class ErrorResponse(
    private val response: Response,
    val errorBody: ResponseBody?
) {

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

    fun errorBodyString(): String {
        if (errorBodyString != null) {
            return errorBodyString!!
        }
        errorBodyString = errorBody?.string() ?: ""
        return errorBodyString ?: ""
        return ""
    }

    override fun toString(): String {
        return response.toString()
    }

}