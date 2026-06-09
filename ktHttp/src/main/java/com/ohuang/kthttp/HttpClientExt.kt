package com.ohuang.kthttp


import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.HttpResponse
import com.ohuang.kthttp.call.await
import com.ohuang.kthttp.call.awaitOrNull
import com.ohuang.kthttp.call.map
import com.ohuang.kthttp.call.throwCancellationException

import com.ohuang.kthttp.call.toHttpResponseCall

import com.ohuang.kthttp.call.toStringHttpResponseCall

import com.ohuang.kthttp.download.DownloadCall
import com.ohuang.kthttp.download.DownloadFileInfoCall
import com.ohuang.kthttp.download.FileInfo
import com.ohuang.kthttp.transform.ResponseConvert
import com.ohuang.kthttp.transform.Transform
import com.ohuang.kthttp.transform.gsonTransForm
import kotlinx.coroutines.delay
import java.io.File
import java.io.RandomAccessFile



/**
 * json数据，使用Gson进行解析, gsonTransFormSetGson()可设置自定义的gson
 */
inline fun <reified T> HttpClient.jsonCall(noinline block: KtHttpRequest.() -> Unit): HttpCall<T> {
    return httpCall(transform = gsonTransForm(), block = block)
}

/**
 *  返回HttpResponse<T> 类型
 */
fun <T> HttpClient.httpResponseCall(
    transform: Transform<T>,
    block: KtHttpRequest.() -> Unit
): HttpCall<HttpResponse<T>> {
    return responseCall(block).toHttpResponseCall(transform)
}

/**
 *  返回HttpResponse<T> 类型
 */
fun <T> HttpClient.httpResponseCall(
    convert: ResponseConvert<T>,
    block: KtHttpRequest.() -> Unit
): HttpCall<HttpResponse<T>> {
    return responseCall(block).toHttpResponseCall(convert)
}


/**
 *  返回HttpResponse<String> 类型
 */
fun HttpClient.stringHttpResponseCall(
    block: KtHttpRequest.() -> Unit
): HttpCall<HttpResponse<String>> {
    return responseCall(block).toStringHttpResponseCall()
}

/**
 * 请求json数据 返回HttpResponse<T> 类型  ,使用Gson进行解析, gsonTransFormSetGson()可设置自定义的gson
 */
inline fun <reified T> HttpClient.jsonHttpResponseCall(
    noinline block: KtHttpRequest.() -> Unit
): HttpCall<HttpResponse<T>> {
    return responseCall(block).toHttpResponseCall(transform = gsonTransForm())
}

/**
 *  下载文件
 *  @param file 文件
 *  @param isContinueDownload 是否断点下载
 *  @param onProcess 下载进度
 *  @param block 请求参数
 *
 */
fun HttpClient.download(
    file: File,
    isContinueDownload: Boolean = false,
    onProcess: (current: Long, total: Long) -> Unit = { _, _ -> },
    block: KtHttpRequest.() -> Unit
): HttpCall<File> {
    var lastIndex = 0L
    if (isContinueDownload && file.exists()) {  //开启断点下载 且 文件存在时
        val randomAccessFile = RandomAccessFile(file, "rw")
        lastIndex = if (randomAccessFile.length() > 1) {
            randomAccessFile.length() - 1//避免文件已完全下载时 导致的服务端416错误
        } else {
            0
        }
    }
    return DownloadCall(
        file = file,
        rangeStart = lastIndex,
        onProcess = onProcess,
        call = responseCall {
            block.invoke(this)
            if (lastIndex > 0) {
                addHeader("Range", "bytes=$lastIndex-")
            }
        }
    )
}

/**
 *  获取下载的文件大小  类型:long 单位:byte
 */
fun HttpClient.downloadFileSize(block: KtHttpRequest.() -> Unit): HttpCall<Long> {
    return DownloadFileInfoCall(call = responseCall(block)).map { it.contentLength }
}




/**
 *  获取下载的文件大小  类型:long 单位:byte
 */
fun HttpClient.downloadFileInfo(block: KtHttpRequest.() -> Unit): HttpCall<FileInfo> {
    return DownloadFileInfoCall(call = responseCall(block))
}

/**
 * 请求失败后重新请求
 */
suspend inline fun <reified T> HttpClient.retryOnFailure(
    count: Int = 3,
    time: Long = 1000,
    call: HttpClient.() -> HttpCall<T>
): T? {
    if (count <= 0 || time <= 0) {
        throw Exception("count <= 0 or time <= 0")
    }
    var num = 0
    while (num < count - 1) {
        try {
            return call(this@retryOnFailure).await()
        } catch (e: Throwable) {
            throwCancellationException(e)
        }
        delay(time)
        num++
    }
    return call(this@retryOnFailure).awaitOrNull()
}

/**
 * 请求失败后重新请求
 */
suspend inline fun <reified T> httpCallRetryOnFailure(
    count: Int = 3,
    time: Long = 1000,
    call: () -> HttpCall<T>
): T? {
    if (count <= 0 || time <= 0) {
        throw Exception("count <= 0 or time <= 0")
    }
    var num = 0
    while (num < count - 1) {
        try {
            return call().await()
        } catch (e: Throwable) {
            throwCancellationException(e)
        }
        delay(time)
        num++
    }
    return call().awaitOrNull()

}