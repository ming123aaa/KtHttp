package com.ohuang.kthttp.call

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.Transform
import okhttp3.Response

open class KtException(msg: String) : Exception(msg)
class CodeNo200Exception(msg: String) : KtException(msg)
class EmptyBodyException(msg: String) : KtException(msg)
class TransformException(msg: String) : KtException(msg)

private const val key_logString = "StringTransformLog"
private const val key_hookString = "StringTransformHook"
fun KtHttpConfig.logString(block: (String) -> Unit) {
    setConfigs(key_logString, object : StringTransformLog {
        override fun log(body: String, response: Response) {
            block(body)
        }
    })
}

fun KtHttpConfig.logString(block: StringTransformLog) {
    setConfigs(key_logString, block)
}

fun interface StringTransformLog {
    fun log(body: String, response: Response)
}
private fun <T> HttpCall<T>.logString( body: String, response: Response) {
    val logString = getConfigs()[key_logString]
    if (logString is StringTransformLog) {
        logString.log( body, response)
    }
}

/**
 * 拦截字符串，返回新的字符串
 */
fun KtHttpConfig.hookString(block: (String) -> String) {
    setConfigs(key_hookString, object : StringTransformHook{
        override fun hook(response: Response): String {
           return block(response.body!!.string())
        }
    })
}
/**
 * 拦截字符串，返回新的字符串
 */
fun KtHttpConfig.hookString(block: StringTransformLog) {
    setConfigs(key_hookString, block)
}


fun interface StringTransformHook {
    fun hook(response: Response):String
}
private fun <T> HttpCall<T>.hookString(response: Response):String {
    val logString = getConfigs()[key_hookString]
    if (logString is StringTransformHook) {
        return logString.hook(response)
    }
    return  response.body!!.string()
}


internal class Code200TransformCall<T>(
    call: HttpCall<Response>,
    private val transform: Transform<T>
) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            var value: T? = null
            try {
                val string =hookString(it)

                logString(string, it)
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

internal class TransformCall<T>(call: HttpCall<Response>, private val transform: Transform<T>) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            var value: T? = null
            try {
                val string = hookString(it)
                logString(string, it)
                value = transform.transform(string)
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