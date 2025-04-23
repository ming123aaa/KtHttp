package com.ohuang.kthttp.call


import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.KtHttpConfigImpl
import com.ohuang.kthttp.transform.Transform
import com.ohuang.kthttp.transform.StringTransForm
import com.ohuang.kthttp.wait.ThreadWait
import com.ohuang.kthttp.wait.WaitResult
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
    return ResponseCall(this, ktHttpConfigImpl.configs)
}

/**
 *  Response转化为String
 */
fun HttpCall<Response>.toStringHttpCall(): HttpCall<String> {
    return toTransformCall(StringTransForm)
}

/**
 *  Response转化为String
 */
fun HttpCall<Response>.toStringHttpCallSafe(): HttpCall<String> {
    return toSafeTransformCall(StringTransForm)
}

fun<T> HttpCall<String>.toTransform(transform: Transform<T>):HttpCall<T>{
    return StringTransformCall(this,transform)
}

/**
 * Response转化为指定类型   只对code==200时处理
 */
fun <T> HttpCall<Response>.toSafeTransformCall(
    transform: Transform<T>
): HttpCall<T> {
    return Code200TransformCall(this, transform)
}

/**
 *  Response转化为指定类型
 */
fun <T> HttpCall<Response>.toTransformCall(
    transform: Transform<T>
): HttpCall<T> {
    return TransformCall(this, transform)
}

/**
 *  会堵塞当前线程，更推荐使用getResult()
 * 等待请求结果, 如果超时则抛出异常
 * @param timeOut 超时时间，单位毫秒，0表示无限制
 */
fun <T> HttpCall<T>.waitResult(timeOut: Long = 0): T {
    val threadWait = ThreadWait<T>()
    request({
        threadWait.setResult(WaitResult(it))
    }, {
        threadWait.setResult(WaitResult(it))
    })
    val waitResult = threadWait.waitResult(timeOut)
    if (waitResult != null) {
        if (waitResult.isSuccess) {
            return waitResult.result
        } else {
            throw waitResult.throwable
        }
    }
    throw Exception("waitResult error")
}

/**
 * 会堵塞当前线程，更推荐使用getResultOrNull()
 * 等待请求结果, 如果超时则抛出异常
 * @param timeOut 超时时间，单位毫秒，0表示无限制
 *
 */
fun <T> HttpCall<T>.waitResultOrNull(timeOut: Long = 0, block: (Throwable) -> Unit = {}): T? {
    return try {
        waitResult(timeOut = timeOut)
    } catch (e: Throwable) {
        block(e)
        null
    }

}
