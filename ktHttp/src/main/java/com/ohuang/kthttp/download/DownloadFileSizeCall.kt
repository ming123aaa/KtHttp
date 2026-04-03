package com.ohuang.kthttp.download

import com.ohuang.kthttp.call.ErrorResponse
import com.ohuang.kthttp.call.ErrorResponseException
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.KtHttpCall
import com.ohuang.kthttp.call.bufferResponseBody
import okhttp3.Response

/**
 *  返回文件大小 Long 单位byte
 */
open class DownloadFileSizeCall(
    call: HttpCall<Response>
) : KtHttpCall<Long, Response>(call) {



    protected fun downloadFileSize(response: Response): Long {
        // 1. 检查响应是否成功
        if (!(response.code == 200 || response.code == 206)) {
            throw ErrorResponseException(
                msg = "HTTP ${response.code}: ${response.message}",
                errorResponse = ErrorResponse(
                    response = response,
                    errorBody = bufferResponseBody(response.body)
                )
            )
        }

        // 2. 获取响应体和总大小
        val responseBody = response.body ?: throw ErrorResponseException(
            msg = "Response body is null",
            errorResponse = ErrorResponse(response = response, errorBody = null)
        )
        val totalSize = responseBody.contentLength() // 文件总大小
        responseBody.byteStream().close()
       return  totalSize
    }





    override fun request(
        error: (Throwable) -> Unit,
        callback: (Long) -> Unit
    ) {
        call.request(error = error) { response ->
            callback.invoke( downloadFileSize(response))//下载完成
        }
    }

}