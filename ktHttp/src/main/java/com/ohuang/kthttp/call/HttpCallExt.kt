package com.ohuang.kthttp.call

import com.ohuang.kthttp.ResponseCall
import com.ohuang.kthttp.Transform
import com.ohuang.kthttp.transform.StringTransForm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Response
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MapHttpCall<B, T>(call: HttpCall<T>, private val transform: (T) -> B) :
    KtHttpCall<B, T>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (B) -> Unit) {
        call.request(error) { callback.invoke(transform.invoke(it)) }
    }
}

fun <T, B> HttpCall<T>.map(
    transform: (T) -> B
): HttpCall<B> {
    return MapHttpCall(this, transform)
}

/**
 * 协程获取结果
 * @param isCancel 协程取消后是否 取消网络请求
 */
suspend fun <T> HttpCall<T>.getResult(isCancel: Boolean = false): T {
    return suspendCancellableCoroutine { continuation ->
        if (isCancel) {
            continuation.invokeOnCancellation { this@getResult.cancel() }
        }
        this@getResult.request({
            if (continuation.isActive) {
                continuation.resumeWithException(it)
            }
        }, {

            if (continuation.isActive) {
                continuation.resume(it)
            }
        })
    }
}

/**
 * 出现异常时回调且返回null
 */
suspend fun <T> HttpCall<T>.getResultSafe(
    isCancel: Boolean = false,
    block: (Throwable) -> Unit = {}
): T? {
    try {
        return getResult(isCancel = isCancel)
    } catch (e: Throwable) {
        if (e is CancellationException) {//处理协程取消异常
            throw e
        } else {
            block(e)
        }
    }
    return null
}

fun <T> HttpCall<T>.asFlow(isCancel: Boolean = false): Flow<T> {
    return kotlinx.coroutines.flow.flow { emit(getResult(isCancel = isCancel)) }
}



fun Call.toHttpCall(): HttpCall<Response> {
    return ResponseCall(this)
}

class CodeNo200Exception(msg: String) : Exception(msg)
class EmptyBodyException(msg: String) : Exception(msg)
class TransformException(msg: String) : Exception(msg)

class TransformCall<T>(call: HttpCall<Response>, private val transform: Transform<T>) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            var value: T? = null
            try {
                val string = it.body!!.string()
                if (it.code != 200) {
                    throw CodeNo200Exception("code is not 200  $it")
                }
                if (string.isNotEmpty()) {
                    value = transform.transform(string)
                } else {
                    throw EmptyBodyException("body string is Empty")
                }
                if (value == null) {
                    throw TransformException("transform error")
                }
            } catch (e: Throwable) {
                error(e)
                return@request
            }
            callback(value)
        })
    }


}

fun HttpCall<Response>.toStringHttpCall(): HttpCall<String> {
    return TransformCall(this, StringTransForm)
}


fun <T> HttpCall<Response>.toHttpCall(
    transform: Transform<T>
): HttpCall<T> {
    return TransformCall(this, transform)
}
