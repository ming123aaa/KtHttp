package com.ohuang.kotlinhttp

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.KtHttpCall
import com.ohuang.kthttp.call.awaitOrNull
import com.ohuang.kthttp.call.waitResult
import com.ohuang.kthttp.call.waitResultOrNull
import kotlinx.coroutines.launch

private var mHandler = Handler(Looper.getMainLooper())

fun <T> HttpCall<T>.toMainHttpCall(): MainHttpCall<T> {
    return MainHttpCall(this)
}

fun interface UnitCallBack<T>{
    fun invoke(data:T)
}

/**
 * 提供一些拓展的request方法,方便在java中使用
 */
class MainHttpCall<T>(call: HttpCall<T>) : KtHttpCall<T, T>(call) {

    class CallBack<T> {

        private var successData: T? = null
        private var errorData: Throwable? = null
        private var success: UnitCallBack<T>? = null
        private var error: UnitCallBack<Throwable>? = null

        fun setResult(data: T){
            this.successData=data
            this.success?.invoke(data)
        }

        fun setError(mThrowable: Throwable){
            this.errorData=mThrowable
            this.error?.invoke(mThrowable)

        }

        fun onSuccess(callBack: UnitCallBack<T>): CallBack<T> {
            success = callBack
            successData?.let {
                this.success?.invoke(it)
            }
            return this
        }

        fun onError(callBack: UnitCallBack<Throwable>): CallBack<T> {
            error = callBack
            errorData?.let {
                this.error?.invoke(it)
            }
            return this
        }

    }

    companion object {
        fun <T> create(call: HttpCall<T>): MainHttpCall<T> {
            return MainHttpCall(call)
        }
    }

    /**
     * 回调运行在主线程
     */
    fun requestOnMainThread(): CallBack<T> {
        val callBack = CallBack<T>()
        request({
            mHandler.post { callBack.setError(it) }
        }, {
            mHandler.post { callBack.setResult(it)}
        })
        return callBack
    }



    fun request(): CallBack<T> {
        val callBack = CallBack<T>()
        call.request({ callBack.setError(it) }, { callBack.setResult(it) })
        return callBack
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



