package com.ohuang.kthttp.call


import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.KtHttpConfigImpl
import com.ohuang.kthttp.transform.Transform
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

/**
 * 类型转化
 */
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
 * 协程获取结果
 *  * @param isCancel 协程取消后是否 取消网络请求
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

/**
 * 协程获取结果
 *  * @param isCancel 协程取消后是否 取消网络请求
 * 出现异常时回调且返回null
 */
suspend fun <T> HttpCall<T>.getResultOrNull(
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

/**
 *  转化为flow
 *  * @param isCancel 协程取消后是否 取消网络请求
 */
fun <T> HttpCall<T>.asFlow(isCancel: Boolean = false): Flow<T> {
    return kotlinx.coroutines.flow.flow { emit(getResult(isCancel = isCancel)) }
}

/**
 *  转化为flow
 *  * @param isCancel 协程取消后是否 取消网络请求
 */
fun <T> HttpCall<T>.asFlowOrNull(isCancel: Boolean = false): Flow<T?> {
    return kotlinx.coroutines.flow.flow { emit(getResultOrNull(isCancel = isCancel)) }
}


fun Call.toHttpCall(block: KtHttpConfig.() -> Unit = {}): HttpCall<Response> {
    val ktHttpConfigImpl = KtHttpConfigImpl()
    block(ktHttpConfigImpl)
    return ResponseCall(this, ktHttpConfigImpl.map)
}

fun HttpCall<Response>.toStringHttpCall(): HttpCall<String> {
    return toTransformCall(StringTransForm)
}


fun <T> HttpCall<Response>.toSafeTransformCall(
    transform: Transform<T>
): HttpCall<T> {
    return Code200TransformCall(this, transform)
}

fun <T> HttpCall<Response>.toTransformCall(
    transform: Transform<T>
): HttpCall<T> {
    return TransformCall(this, transform)
}
