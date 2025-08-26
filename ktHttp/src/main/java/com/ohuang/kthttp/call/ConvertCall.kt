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
 *  Response转化为String
 *  只处理httpCode==200
 */
fun HttpCall<Response>.toStringHttpCallCode200(): HttpCall<String> {
    return toTransformCallCode200(StringTransForm)
}

/**
 *  Response转化为String
 *  只处理httpCode==200
 */
@Deprecated(
    message = "请替换成toStringHttpCallCode200",
    replaceWith = ReplaceWith("toStringHttpCallCode200()")
)
fun HttpCall<Response>.toStringHttpCallSafe(): HttpCall<String> {
    return toStringHttpCallCode200()
}

/**
 *  Response转化为String
 *  不检查httpCode
 */
fun HttpCall<Response>.toStringHttpCallNotCheck(): HttpCall<String> {
    return toTransformCallNotCheck(StringTransForm)
}


/**
 * 转化为指定类型
 */
fun <T> HttpCall<String>.toTransform(transform: Transform<T>): HttpCall<T> {
    return StringTransformCall(this, transform)
}

/**
 *  Response转化为指定类型
 *  只处理 httpCode==200
 */
fun <T> HttpCall<Response>.toTransformCallCode200(
    transform: Transform<T>
): HttpCall<T> {
    return ConvertCall(
        call = this,
        codeCheck = CodeCheck.Code_200,
        convert = transform.toConvert(this)
    )
}


/**
 *  Response转化为指定类型
 *  只处理 httpCode==200
 */
fun <T> HttpCall<Response>.toConvertCallCode200(
    convert: ResponseConvert<T>
): HttpCall<T> {
    return ConvertCall(call = this, codeCheck = CodeCheck.Code_200, convert = convert)
}

/**
 *  Response转化为指定类型
 *  只处理 httpCode==200
 */
@Deprecated(
    "请替换成toTransformCallCode200()",
    replaceWith = ReplaceWith("toTransformCallCode200(transform)"),
    level = DeprecationLevel.WARNING
)
fun <T> HttpCall<Response>.toSafeTransformCall(
    transform: Transform<T>
): HttpCall<T> {
    return toTransformCallCode200(transform)
}

/**
 * Response转化为指定类型
 * 不检查httpCode
 */
fun <T> HttpCall<Response>.toTransformCallNotCheck(
    transform: Transform<T>
): HttpCall<T> {
    return ConvertCall(
        call = this,
        codeCheck = CodeCheck.Code_NotCheck,
        convert = transform.toConvert(this)
    )
}

/**
 * Response转化为指定类型
 * 不检查httpCode
 */
fun <T> HttpCall<Response>.toConvertCallNotCheck(
    convert: ResponseConvert<T>
): HttpCall<T> {
    return ConvertCall(call = this, codeCheck = CodeCheck.Code_NotCheck, convert = convert)
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
        codeCheck = CodeCheck.Code_Successful,
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
    return ConvertCall(call = this, codeCheck = CodeCheck.Code_Successful, convert = convert)
}

fun <T> Transform<T>.toConvert(httpCall: HttpCall<*>): ResponseConvert<T> {
    return TransformConvert(httpCall = httpCall, transform = this)
}

internal class TransformConvert<T>(val httpCall: HttpCall<*>, val transform: Transform<T>) :
    ResponseConvert<T> {
    override fun convert(response: Response): T? {
        val string = httpCall.hookStringBody(response)
        httpCall.onStringBody(string, response)
        var value: T? = null
        if (string.isNotEmpty()) {
            value = transform.transform(string)
        } else {
            throw EmptyBodyException(
                msg = "body string is Empty",
                errorResponse = ErrorResponse(response = response, errorBody = null)
            )
        }
        if (value == null) {
            throw TransformException(msg = "transform error", content = string)
        }
        return value
    }
}

private fun buffer(body: ResponseBody?): ResponseBody? {
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

internal class ConvertCall<T>(
    call: HttpCall<Response>,
    private var codeCheck: CodeCheck = CodeCheck.Code_Successful,
    private val convert: ResponseConvert<T>
) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            checkHttpCode(it)
            var value = convert.convert(it)
            if (value == null) {
                throw ErrorResponseException(
                    msg = "convert error " + convert.javaClass.name,
                    errorResponse = ErrorResponse(response = it, errorBody = null)
                )
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
