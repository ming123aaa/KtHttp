package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.getConfigForType
import okhttp3.Response

private const val key_onStringBody = "StringTransformShow"

fun interface StringTransformShow {
    fun onStringBody(body: String, response: Response)
}

/**
 *  查看body
 *   @param isOverride 是否覆盖原有的回调
 *
 */
fun KtHttpConfig.onStringBody(isOverride: Boolean = false, block: (String) -> Unit) {

    onStringBody(isOverride =  isOverride, block={body: String, response: Response->
        block(body)
    })
}

/**
 *  查看body
 *   @param isOverride 是否覆盖原有的回调
 */
fun KtHttpConfig.onStringBody(
    isOverride: Boolean = false,
    block: (body: String, response: Response) -> Unit
) {
    var stringTransformShow = object : StringTransformShow {
        override fun onStringBody(body: String, response: Response) {
            block(body, response)
        }
    }
    if (!isOverride) {
        var old = getConfigForType<StringTransformShow>(key_onStringBody)
        if (old != null) {
            object : StringTransformShow {
                override fun onStringBody(body: String, response: Response) {
                    old.onStringBody(body, response)
                    block(body, response)
                }
            }
        }
    }

    setConfig(key_onStringBody, stringTransformShow)
}


internal fun <T> HttpCall<T>.onStringBody(body: String, response: Response) {
    val logString = getConfigs()[key_onStringBody]
    if (logString is StringTransformShow) {
        logString.onStringBody(body, response)
    }
}
