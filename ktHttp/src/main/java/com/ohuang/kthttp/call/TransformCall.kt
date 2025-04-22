package com.ohuang.kthttp.call

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.transform.Transform
import okhttp3.Response

open class KtException(msg: String) : Exception(msg)
class CodeNo200Exception(msg: String) : KtException(msg)
class EmptyBodyException(msg: String) : KtException(msg)
class TransformException(msg: String) : KtException(msg)

private const val key_showStringBody = "StringTransformShow"
private const val key_hookStringBody = "StringTransformHook"

/**
 *  查看body
 */
fun KtHttpConfig.showStringBody(block: (String) -> Unit) {
    setConfig(key_showStringBody, object : StringTransformShow {
        override fun show(body: String, response: Response) {
            block(body)
        }
    })
}
/**
 *  查看body转String的结果
 */
fun KtHttpConfig.showStringBody(block: StringTransformShow) {
    setConfig(key_showStringBody, block)
}

fun interface StringTransformShow {
    fun show(body: String, response: Response)
}
private fun <T> HttpCall<T>.showStringBody(body: String, response: Response) {
    val logString = getConfigs()[key_showStringBody]
    if (logString is StringTransformShow) {
        logString.show( body, response)
    }
}

/**
 * 拦截Body字符串，返回新的字符串
 */
fun KtHttpConfig.hookStringBody(block: (String) -> String) {
    setConfig(key_hookStringBody, object : StringTransformHook{
        override fun hook(response: Response): String {
           return block(response.body!!.string())
        }
    })
}
/**
 * 拦截字符串，返回新的字符串
 */
fun KtHttpConfig.hookStringBody(block: StringTransformShow) {
    setConfig(key_hookStringBody, block)
}


fun interface StringTransformHook {
    fun hook(response: Response):String
}
private fun <T> HttpCall<T>.hookStringBody(response: Response):String {
    val logString = getConfigs()[key_hookStringBody]
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
                if (it.code != 200) {
                    throw CodeNo200Exception("code is not 200  $it")
                }
                val string =hookStringBody(it)
                showStringBody(string, it)
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
                val string = hookStringBody(it)
                showStringBody(string, it)
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