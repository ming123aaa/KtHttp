package com.ohuang.kthttp.call

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.transform.Transform
import okhttp3.Response

open class KtException(msg: String) : Exception(msg)
class CodeNo200Exception(msg: String) : KtException(msg)
class EmptyBodyException(msg: String) : KtException(msg)
class TransformException(msg: String) : KtException(msg)

private const val key_onStringBody = "StringTransformShow"
private const val key_hookStringBody = "StringTransformHook"

/**
 *  查看body
 */
fun KtHttpConfig.onStringBody(block: (String) -> Unit) {
    setConfig(key_onStringBody, object : StringTransformShow {
        override fun onStringBody(body: String, response: Response) {
            block(body)
        }
    })
}

/**
 *  查看body
 */
fun KtHttpConfig.onStringBody(block: StringTransformShow) {
    setConfig(key_onStringBody, block)
}

fun interface StringTransformShow {
    fun onStringBody(body: String, response: Response)
}

internal fun <T> HttpCall<T>.onStringBody(body: String, response: Response) {
    val logString = getConfigs()[key_onStringBody]
    if (logString is StringTransformShow) {
        logString.onStringBody(body, response)
    }
}

/**
 * 拦截Body字符串，返回新的字符串
 */
fun KtHttpConfig.hookStringBody(block: (String) -> String) {
    setConfig(key_hookStringBody, object : StringTransformHook {
        override fun hook(response: Response): String {
            return block(response.body!!.string())
        }
    })
}

/**
 * 拦截Body字符串，返回新的字符串
 */
fun KtHttpConfig.hookStringBody(block: StringTransformShow) {
    setConfig(key_hookStringBody, block)
}


fun interface StringTransformHook {
    fun hook(response: Response): String
}

internal fun <T> HttpCall<T>.hookStringBody(response: Response): String {
    val logString = getConfigs()[key_hookStringBody]
    if (logString is StringTransformHook) {
        return logString.hook(response)
    }
    return response.body!!.string()
}

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