package com.ohuang.kthttp.call

import com.ohuang.kthttp.config.hookStringBody
import com.ohuang.kthttp.config.onStringBody
import com.ohuang.kthttp.transform.Transform
import okhttp3.Response

open class KtException(msg: String) : Exception(msg)
class CodeNo200Exception(msg: String) : KtException(msg)
class EmptyBodyException(msg: String) : KtException(msg)
class TransformException(msg: String) : KtException(msg)




internal class StringTransformCall<T>(call: HttpCall<String>, private val transform: Transform<T>) :
    KtHttpCall<T, String>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error) {
            var value: T? = null
            value = transform.transform(it)
            if (value == null) {
                throw TransformException("transform error")
            }
            callback(value)
        }
    }


}


internal class Code200TransformCall<T>(
    call: HttpCall<Response>,
    private val transform: Transform<T>
) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            var value: T? = null
            if (it.code != 200) {
                throw CodeNo200Exception("code is not 200  $it")
            }
            val string = hookStringBody(it)
            onStringBody(string, it)
            if (string.isNotEmpty()) {
                value = transform.transform(string)
            } else {
                throw EmptyBodyException("body string is Empty")
            }
            if (value == null) {
                throw TransformException("transform error")
            }
            callback(value)
        })
    }
}

internal class TransformCall<T>(call: HttpCall<Response>, private val transform: Transform<T>) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            var value: T? = null
            val string = hookStringBody(it)
            onStringBody(string, it)
            value = transform.transform(string)
            if (value == null) {
                throw TransformException("transform error")
            }
            callback(value)
        })
    }
}