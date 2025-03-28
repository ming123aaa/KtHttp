package com.ohuang.kotlinhttp

import android.os.Handler
import android.os.Looper
import com.ohuang.kthttp.ResponseCall
import com.ohuang.kthttp.call.EmptyBodyException
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.KtHttpCall
import okhttp3.Response
import java.io.File

private var mHandler = Handler(Looper.getMainLooper())
fun <T> HttpCall<T>.requestOnMainThread(error: (Throwable) -> Unit = {}, callback: (T) -> Unit) {

    request({
        mHandler.post { error.invoke(it) }
    }, {
        mHandler.post { callback.invoke(it) }
    })

}

fun HttpCall<Response>.toFileCall(file: File): FileHttpCall {
    return FileHttpCall(file,this)
}

class FileHttpCall(private var file: File, call: HttpCall<Response>) : KtHttpCall<File, Response>(call) {

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