package com.ohuang.kthttp.download

import com.ohuang.kthttp.call.ErrorResponse
import com.ohuang.kthttp.call.ErrorResponseException
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.KtHttpCall
import com.ohuang.kthttp.call.bufferResponseBody
import okhttp3.MediaType
import okhttp3.Response
import java.io.File


class FileInfo(
    val contentLength: Long,
    val fileName: String?,
    val contentType: MediaType?
)

/**
 *  返回文件大小 Long 单位byte
 */
open class DownloadFileInfoCall(
    call: HttpCall<Response>
) : KtHttpCall<FileInfo, Response>(call) {

    protected fun downloadFileInfo(response: Response): FileInfo {
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

        val disposition = response.header("content-disposition", "")
        val fileName = disposition?.split(";")?.filter { it.startsWith("filename=") }
            ?.map { it.replace("filename=", "") }?.firstOrNull()
        // 2. 获取响应体和总大小
        val responseBody = response.body ?: throw ErrorResponseException(
            msg = "Response body is null",
            errorResponse = ErrorResponse(response = response, errorBody = null)
        )
        val contentType = responseBody.contentType()
        val totalSize = responseBody.contentLength() // 文件总大小

        responseBody.byteStream().close()
        return FileInfo(contentLength = totalSize,fileName=fileName,contentType=contentType)
    }


    override fun request(
        error: (Throwable) -> Unit,
        callback: (FileInfo) -> Unit
    ) {
        call.request(error = error) { response ->
            callback.invoke(downloadFileInfo(response))//下载完成
        }
    }

}

