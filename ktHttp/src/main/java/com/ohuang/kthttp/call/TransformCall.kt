package com.ohuang.kthttp.call

import com.ohuang.kthttp.config.hookStringBody
import com.ohuang.kthttp.config.onStringBody
import com.ohuang.kthttp.transform.Transform
import okhttp3.Response

open class KtHttpException(msg: String) : Exception(msg)
class CodeNot200Exception(msg: String) : KtHttpException(msg)
class EmptyBodyException(msg: String) : KtHttpException(msg)
class TransformException(msg: String) : KtHttpException(msg)




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

internal enum class CodeCheck{
    Code_Successful,
    Code_200,
    Code_NotCheck
}

internal class TransformCall<T>(
    call: HttpCall<Response>,
    private var codeCheck:CodeCheck=CodeCheck.Code_Successful,
    private val transform: Transform<T>
) :
    KtHttpCall<T, Response>(call) {
    override fun request(error: (Throwable) -> Unit, callback: (T) -> Unit) {
        call.request(error = error, callback = {
            var value: T? = null
            checkHttpCode(it)
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

    private fun checkHttpCode(response: Response) {
        if (codeCheck==CodeCheck.Code_NotCheck){
            return
        }
        val isThrow=if (codeCheck== CodeCheck.Code_Successful){
            !response.isSuccessful
        }else if (codeCheck== CodeCheck.Code_200){
            response.code!=200
        }else{
            false
        }
        if (isThrow) {
            try {
                response.close()
            } finally {
                if (codeCheck== CodeCheck.Code_200){
                    throw CodeNot200Exception("http code!=200  $response")
                }else {
                    throw KtHttpException("http code:${response.code}  $response")
                }
            }
        }
    }
}
