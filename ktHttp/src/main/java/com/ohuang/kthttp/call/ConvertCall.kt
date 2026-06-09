package com.ohuang.kthttp.call

import com.ohuang.kthttp.config.hookStringBody
import com.ohuang.kthttp.config.onStringBody
import com.ohuang.kthttp.transform.ResponseConvert
import com.ohuang.kthttp.transform.StringTransForm
import com.ohuang.kthttp.transform.Transform
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer


/**
 *  Response转化为String
 *
 *  处理httpCode>=200&&httpCode<300的请求结果
 */

fun HttpCall<Response>.toStringHttpCall(): HttpCall<String> {
    return toTransformCall(StringTransForm)
}


/**
 * 转化为指定类型
 */
fun <T> HttpCall<String>.toTransform(transform: Transform<T>): HttpCall<T> {
    return StringTransformCall(this, transform)
}

/**
 *  Response转化为指定类型
 *  处理httpCode>=200&&httpCode<300的请求结果
 */
fun <T> HttpCall<Response>.toTransformCall(
    transform: Transform<T>
): HttpCall<T> {
    return ConvertCall(
        call = this,
        convert = transform.toConvert(this)
    )
}

/**
 *  Response转化为指定类型
 *  处理httpCode>=200&&httpCode<300的请求结果
 */
fun <T> HttpCall<Response>.toConvertCall(
    convert: ResponseConvert<T>
): HttpCall<T> {
    return ConvertCall(call = this, convert = convert)
}

fun <T> Transform<T>.toConvert(httpCall: HttpCall<*>): ResponseConvert<T> {
    return TransformConvert(httpCall = httpCall, transform = this)
}

internal class TransformConvert<T>(val httpCall: HttpCall<*>, val transform: Transform<T>) :
    ResponseConvert<T> {
    override fun convert(response: Response): T {
        val string = httpCall.hookStringBody(response)
        httpCall.onStringBody(string, response)
        var value: T? = null
        value = transform.transform(string)
        try {
            response.close()
        } catch (e: Throwable) {
        }
        return value
    }
}

internal fun bufferResponseBody(body: ResponseBody?): ResponseBody? {
    if (body == null) {
        return null
    }
    val buffer: Buffer = Buffer()
    body.source().readAll(buffer)
    return buffer.asResponseBody(body.contentType(), body.contentLength())
}


internal class StringTransformCall<T>(call: HttpCall<String>, private val transform: Transform<T>) :
    KtHttpCall<T, String>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error) {
            var value: T? = null
            value = transform.transform(it)
            callback(value)
        }
    }
}


internal class ConvertCall<T>(
    call: HttpCall<Response>,
    private val convert: ResponseConvert<T>
) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            checkHttpCode(it)
            var value: T? = null
            value = convert.convert(it)
            callback(value)
        })
    }

    private fun checkHttpCode(response: Response) {

        val isThrow = !response.isSuccessful

        if (isThrow) {
            var errorResponse = ErrorResponse(response = response, errorBody = null)
            try {
                errorResponse =
                    ErrorResponse(
                        response = response,
                        errorBody = bufferResponseBody(response.body)
                    )
                response.close()
            } finally {
                throw ErrorResponseException(
                    msg = "http code:${response.code}  $response",
                    errorResponse = errorResponse
                )

            }
        }
    }
}
