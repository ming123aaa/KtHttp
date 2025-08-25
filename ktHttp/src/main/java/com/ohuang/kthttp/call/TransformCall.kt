package com.ohuang.kthttp.call

import com.ohuang.kthttp.config.hookStringBody
import com.ohuang.kthttp.config.onStringBody
import com.ohuang.kthttp.transform.Transform
import okhttp3.Headers
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException


class ErrorResponse(private val response: Response, val errorBody: ResponseBody?) {

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

open class KtHttpException(msg: String) : Exception(msg)
open class ErrorResponseException(msg: String, val errorResponse: ErrorResponse) :
    KtHttpException(msg)

class CodeNot200Exception(msg: String, errorResponse: ErrorResponse) : ErrorResponseException(
    msg,
    errorResponse
)

class EmptyBodyException(msg: String, errorResponse: ErrorResponse) : ErrorResponseException(
    msg,
    errorResponse
)

class TransformException(msg: String, val content: String) : KtHttpException(msg)


private fun buffer(body: ResponseBody?): ResponseBody? {
    if (body == null) {
        return null
    }
    val buffer: Buffer = Buffer()
    body.source().readAll(buffer)
    return ResponseBody.create(body.contentType(), body.contentLength(), buffer)
}


internal class StringTransformCall<T>(call: HttpCall<String>, private val transform: Transform<T>) :
    KtHttpCall<T, String>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error) {
            var value: T? = null
            value = transform.transform(it)
            if (value == null) {
                throw TransformException(msg = "transform error", content = it)
            }
            callback(value)
        }
    }


}

internal enum class CodeCheck {
    Code_Successful,
    Code_200,
    Code_NotCheck
}

internal class TransformCall<T>(
    call: HttpCall<Response>,
    private var codeCheck: CodeCheck = CodeCheck.Code_Successful,
    private val transform: Transform<T>
) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            var value: T? = null
            checkHttpCode(it)
            val string = hookStringBody(it)
            onStringBody(string, it)
            if (string.isNotEmpty()) {
                value = transform.transform(string)
            } else {
                throw EmptyBodyException(
                    msg = "body string is Empty",
                    errorResponse = ErrorResponse(response = it, errorBody = null)
                )
            }
            if (value == null) {
                throw TransformException(msg = "transform error", content = string)
            }
            callback(value)
        })
    }

    private fun checkHttpCode(response: Response) {
        if (codeCheck == CodeCheck.Code_NotCheck) {
            return
        }
        val isThrow = if (codeCheck == CodeCheck.Code_Successful) {
            !response.isSuccessful
        } else if (codeCheck == CodeCheck.Code_200) {
            response.code != 200
        } else {
            false
        }
        if (isThrow) {
            var errorResponse = ErrorResponse(response = response, errorBody = null)
            try {
                errorResponse =
                    ErrorResponse(response = response, errorBody = buffer(response.body))
                response.close()
            } finally {
                if (codeCheck == CodeCheck.Code_200) {
                    throw CodeNot200Exception(
                        msg = "http code!=200  $response",
                        errorResponse = errorResponse
                    )
                } else {
                    throw ErrorResponseException(
                        msg = "http code:${response.code}  $response",
                        errorResponse = errorResponse
                    )
                }
            }
        }
    }
}
