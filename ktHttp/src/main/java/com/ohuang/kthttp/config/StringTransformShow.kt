package com.ohuang.kthttp.config

import com.ohuang.kthttp.KtHttpConfig
import com.ohuang.kthttp.call.HttpCall
import okhttp3.Response

private const val key_onStringBody = "StringTransformShow"
fun interface StringTransformShow {
    fun onStringBody(body: String, response: Response)
}
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



internal fun <T> HttpCall<T>.onStringBody(body: String, response: Response) {
    val logString = getConfigs()[key_onStringBody]
    if (logString is StringTransformShow) {
        logString.onStringBody(body, response)
    }
}
