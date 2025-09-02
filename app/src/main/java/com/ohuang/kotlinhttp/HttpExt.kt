package com.ohuang.kotlinhttp

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.ohuang.kthttp.call.EmptyBodyException
import com.ohuang.kthttp.call.ErrorResponse
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.KtHttpCall
import com.ohuang.kthttp.call.getResultSafe
import com.ohuang.kthttp.call.waitResult
import com.ohuang.kthttp.call.waitResultOrNull
import kotlinx.coroutines.launch
import okhttp3.Response
import java.io.File

private var mHandler = Handler(Looper.getMainLooper())

fun <T> HttpCall<T>.toMainHttpCall(): MainHttpCall<T> {
    return MainHttpCall(this)
}

/**
 * 提供一些拓展的request方法,方便在java中使用
 */
class MainHttpCall<T>(call: HttpCall<T>) : KtHttpCall<T, T>(call) {

    companion object{
        fun<T> create(call: HttpCall<T>): MainHttpCall<T> {
            return MainHttpCall(call)
        }
    }

    /**
     * 回调运行在主线程
     */
    fun requestOnMainThread(error: (Throwable) -> Unit = {}, callback: (T) -> Unit) {
        request({
            mHandler.post { error.invoke(it) }
        }, {
            mHandler.post { callback.invoke(it) }
        })
    }

    /**
     * 回调运行在主线程,并绑定生命周期
     */
    fun requestOnActivity(
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
     * 回调在主线程,livedata方式
     */
    fun requestOnLivedata(error: (Throwable) -> Unit = {}, livedata: MutableLiveData<T>) {
        request({
            mHandler.post { error.invoke(it) }
        }, {
            livedata.postValue(it)
        })
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



