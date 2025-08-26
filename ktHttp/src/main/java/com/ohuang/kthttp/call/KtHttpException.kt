package com.ohuang.kthttp.call

import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody


open class KtHttpException(msg: String) : Exception(msg)
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

class TransformException(msg: String, val content: String) : KtHttpException(msg)


class ErrorResponse(
    private val response: Response,
    val errorBody: ResponseBody?
) {

    fun code(): Int {
        return response.code
    }

    fun message(): String {
        return response.message
    }

    fun headers(): Headers {
        return response.headers
    }

    fun errorBodyString(): String {
        return errorBody?.string() ?: ""
    }

    override fun toString(): String {
        return response.toString()
    }

}