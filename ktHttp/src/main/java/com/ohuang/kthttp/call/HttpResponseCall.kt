package com.ohuang.kthttp.call

import com.ohuang.kthttp.HttpClient
import com.ohuang.kthttp.KtHttpRequest
import com.ohuang.kthttp.transform.ResponseConvert
import com.ohuang.kthttp.transform.StringTransForm
import com.ohuang.kthttp.transform.Transform
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import okio.Buffer
import okio.BufferedSource

/**
 *  获取HttpResponse.body
 *   HttpResponse<T>转换成T型型
 */
fun <T> HttpCall<HttpResponse<T>>.toBodyCall(): HttpCall<T> {
    return this.map {
        if (it.body != null) {
            return@map it.body
        }
        throw EmptyBodyException(
            msg = "KtResponse body is null",
            errorResponse = ErrorResponse(response = it.raw(), errorBody = it.errorBody)
        )
    }
}

/**
 *  响应转换成HttpResponse<T>
 */
fun <T> HttpCall<Response>.toHttpResponseCall(transform: Transform<T>): HttpCall<HttpResponse<T>> {
    return HttpResponseCall(this, transform.toConvert(this))
}
/**
 *  响应转换成HttpResponse<T>
 */
fun <T> HttpCall<Response>.toHttpResponseCall(transform: ResponseConvert<T>): HttpCall<HttpResponse<T>> {
    return HttpResponseCall(this, transform)
}

/**
 *  返回HttpResponse<String> 类型
 */
fun HttpCall<Response>.toStringHttpResponseCall(): HttpCall<HttpResponse<String>> {
    return HttpResponseCall(this, StringTransForm.toConvert(this))
}



internal class HttpResponseCall<T>(call: HttpCall<Response>, private val responseConvert: ResponseConvert<T>) :
    KtHttpCall<HttpResponse<T>, Response>(call) {
    override fun request(
        error: (Throwable) -> Unit,
        callback: (HttpResponse<T>) -> Unit
    ) {
        call.request(error = error, callback = { mResponse ->
            val rawBody: ResponseBody = mResponse.body!!
            // Remove the body's source (the only stateful object) so we can pass the response along.
            var rawResponse =
                mResponse
                    .newBuilder()
                    .body(NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
                    .build()
            val code = rawResponse.code
            if (code < 200 || code >= 300) {
                var mHttpResponse = HttpResponse<T>(rawResponse, null, null)
                try {
                    // Buffer the entire body to avoid future I/O.
                    val bufferedBody: ResponseBody? = buffer(rawBody)
                    mHttpResponse = HttpResponse<T>(rawResponse, null, bufferedBody)
                } finally {
                    rawBody.close()
                }
                callback(mHttpResponse)
                return@request
            }
            if (code == 204 || code == 205) {
                var mHttpResponse = HttpResponse<T>(rawResponse, null, null)
                rawBody.close()
                callback(mHttpResponse)
                return@request
            }

            var value: T? = null
            value=responseConvert.convert(mResponse)
            callback(HttpResponse<T>(rawResponse, value, null))
        })
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

internal class NoContentResponseBody(
    private val contentType: MediaType?,
    private val contentLength: Long
) : ResponseBody() {

    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        return contentLength
    }

    override fun source(): BufferedSource {
        throw IllegalStateException("Cannot read raw response body of a converted body.")
    }
}