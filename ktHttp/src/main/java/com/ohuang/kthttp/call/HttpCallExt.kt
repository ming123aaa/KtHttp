package com.ohuang.kthttp.call


import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.KtHttpConfigImpl
import com.ohuang.kthttp.download.DownloadCall
import com.ohuang.kthttp.wait.ThreadWait
import com.ohuang.kthttp.wait.WaitResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Response
import java.io.File
import java.io.RandomAccessFile
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
suspend fun <T> HttpCall<T>.awaitOrNull(
    isCancel: Boolean = true,
    block: (Throwable) -> Unit = {}
): T? {
    try {
        return await(isCancel = isCancel)
    } catch (e: Throwable) {
        block(e)
        throwCancellationException(e)

    }
    return null
}

/**
 * 协程获取结果
 * @param isCancel 协程取消后是否 取消网络请求
 */
suspend fun <T> HttpCall<T>.await(
    isCancel: Boolean = true
): T {
    return suspendCancellableCoroutine { continuation ->
        if (isCancel) {
            continuation.invokeOnCancellation {
                this@await.cancel()
            }
        }
        this@await.request(error = {
            if (continuation.isActive) {
                continuation.resumeWithException(it)
            }
        }, callback = {

            if (continuation.isActive) {
                continuation.resume(it)
            }
        })
    }
}

/**
 *  转化为flow
 *  * @param isCancel 协程取消后是否 取消网络请求
 */
fun <T> HttpCall<T>.asFlow(isCancel: Boolean = true): Flow<T> {
    return kotlinx.coroutines.flow.flow { emit(await(isCancel = isCancel)) }
}

/**
 *  转化为flow
 *  * @param isCancel 协程取消后是否 取消网络请求
 */
fun <T> HttpCall<T>.asFlowOrNull(isCancel: Boolean = true): Flow<T?> {
    return kotlinx.coroutines.flow.flow { emit(awaitOrNull(isCancel = isCancel)) }
}


fun Call.toHttpCall(block: KtHttpConfig.() -> Unit = {}): HttpCall<Response> {
    val ktHttpConfigImpl = KtHttpConfigImpl()
    block(ktHttpConfigImpl)
    return ResponseCall(this, ktHttpConfigImpl.configs)
}


/**
 * 会堵塞当前线程
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
 * 会堵塞当前线程
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

fun HttpCall<Response>.download(
    file: File,
    isContinueDownload: Boolean = false,
    onProcess: (current: Long, total: Long) -> Unit = { _, _ -> },
): HttpCall<File> {
    var lastIndex = 0L
    if (isContinueDownload && file.exists()) {  //开启断点下载 且 文件存在时
        val randomAccessFile = RandomAccessFile(file, "rw")
        lastIndex = if (randomAccessFile.length() > 1) {
            randomAccessFile.length() - 1//避免文件已完全下载时 导致的服务端416错误
        } else {
            0
        }
    }
    return DownloadCall(
        file = file,
        rangeStart = lastIndex,
        onProcess = onProcess,
        call = this
    )
}


/**
 * 抛出 协程取消异常
 */
inline fun throwCancellationException(e: Throwable, call: (e: Throwable) -> Unit = {}) {
    if (e is CancellationException) {
        throw e
    }
    call(e)
}

