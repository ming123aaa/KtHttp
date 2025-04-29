package com.ohuang.kotlinhttp

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.ohuang.kthttp.call.EmptyBodyException
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.KtHttpCall
import com.ohuang.kthttp.call.getResultSafe
import com.ohuang.kthttp.call.waitResult
import com.ohuang.kthttp.call.waitResultOrNull
import kotlinx.coroutines.launch
import okhttp3.Response
import java.io.File

private var mHandler = Handler(Looper.getMainLooper())

/**
 * 回调结果运行在主线程
 */
fun <T> HttpCall<T>.requestOnMainThread(error: (Throwable) -> Unit = {}, callback: (T) -> Unit) {

    request({
        mHandler.post { error.invoke(it) }
    }, {
        mHandler.post { callback.invoke(it) }
    })

}

/**
 *
 * 绑定生命周期，回调结果运行在主线程
 */
fun <T> HttpCall<T>.requestOnActivity(
    lifecycleOwner: LifecycleOwner,
    error: (Throwable) -> Unit = {},
    callback: (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        val result = getResultSafe { error.invoke(it) }
        if (result != null) {
            callback.invoke(result)
        }
    }
}

/**
 * 使用livedata返回结果
 */
fun <T> HttpCall<T>.requestOnLivedata(
    error: (Throwable) -> Unit = {},
    livedata: MutableLiveData<T>
) {
    request({
        mHandler.post { error.invoke(it) }
    }, {
        livedata.postValue(it)
    })
}

fun <T> HttpCall<T>.toMainHttpCall(): MainHttpCall<T> {
    return MainHttpCall(this)
}

/**
 * 提供一些拓展的request方法
 */
class MainHttpCall<T>(call: HttpCall<T>) : KtHttpCall<T, T>(call) {

    fun requestOnMainThread(error: (Throwable) -> Unit = {}, callback: (T) -> Unit) {
        call.requestOnMainThread(error, callback)
    }

    fun requestOnActivity(
        lifecycleOwner: LifecycleOwner,
        error: (Throwable) -> Unit = {},
        callback: (T) -> Unit
    ) {
        call.requestOnActivity(lifecycleOwner, error, callback)
    }

    fun requestOnLivedata(error: (Throwable) -> Unit = {}, livedata: MutableLiveData<T>) {
        call.requestOnLivedata(error, livedata)
    }

    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error, callback)
    }

    fun waitResult(timeOut: Long = 0): T {
        return call.waitResult(timeOut)
    }

    fun waitResultOrNull(timeOut: Long = 0): T? {
        return call.waitResultOrNull(timeOut)
    }


}

/**
 * 文件下载
 */
fun HttpCall<Response>.toFileCall(file: File): FileHttpCall {
    return FileHttpCall(file, this)
}

/**
 *  文件下载
 */
class FileHttpCall(private var file: File, call: HttpCall<Response>) :
    KtHttpCall<File, Response>(call) {

    override fun request(error: (Throwable) -> Unit, callback: (File) -> Unit) {
        call.request(error = error) { response ->
            var byteStream = response.body?.byteStream()
            if (byteStream != null) {
                byteStream.use {
                    if (!file.exists()) {
                        if (file.getParentFile() != null) {
                            file.getParentFile()?.mkdirs();
                        }
                        file.createNewFile();
                    }
                    file.outputStream().use { output ->
                        it.copyTo(output) // 使用缓冲流自动复制
                    }
                    callback.invoke(file)
                }
            } else {
                error(EmptyBodyException("body is null"))
                return@request
            }
        }
    }
}

