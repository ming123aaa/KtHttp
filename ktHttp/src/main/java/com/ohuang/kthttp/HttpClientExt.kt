package com.ohuang.kthttp


import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.toStringHttpCallCode200
import com.ohuang.kthttp.call.toStringHttpCallNotCheck
import com.ohuang.kthttp.call.toTransformCallCode200
import com.ohuang.kthttp.call.toTransformCallNotCheck
import com.ohuang.kthttp.download.DownloadCall
import com.ohuang.kthttp.transform.Transform
import java.io.File
import java.io.RandomAccessFile

/**
 * 网络请求，获取字符串内容
 * 不检查httpCode
 * @param block 请求参数
 */
fun HttpClient.stringCallNotCheck(block: KtHttpRequest.() -> Unit): HttpCall<String> {
    return responseCall(block).toStringHttpCallNotCheck()
}

/**
 * 网络请求，获取字符串内容
 * code==200 才会正确返回结果
 * @param block 请求参数
 */
@Deprecated(
    message = "请替换成stringCallCode200()",
    replaceWith = ReplaceWith(expression = "stringCallCode200(block)"),
    level = DeprecationLevel.WARNING
)
fun HttpClient.stringCallSafe(block: KtHttpRequest.() -> Unit): HttpCall<String> {
    return stringCallCode200(block)
}

/**
 * 网络请求，获取字符串内容
 * 只处理httpCode==200
 * @param block 请求参数
 */
fun HttpClient.stringCallCode200(block: KtHttpRequest.() -> Unit): HttpCall<String> {
    return responseCall(block).toStringHttpCallCode200()
}


/**
 * 网络请求，获取对象内容
 * 只处理httpCode==200
 * @param transform 类型转换器
 * @param block 请求参数
 */
fun <T> HttpClient.httpCallCode200(
    transform: Transform<T>,
    block: KtHttpRequest.() -> Unit
): HttpCall<T> {
    return responseCall(block).toTransformCallCode200(transform)
}

/**
 * 网络请求，获取对象内容
 * 不检查httpCode
 * @param transform 类型转换器
 * @param block 请求参数
 */
fun <T> HttpClient.httpCallNotCheck(
    transform: Transform<T>,
    block: KtHttpRequest.() -> Unit
): HttpCall<T> {
    return responseCall(block).toTransformCallNotCheck(transform)
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
): DownloadCall {
    var lastIndex = 0L
    if (isContinueDownload) {
        val randomAccessFile = RandomAccessFile(file, "rw")
        lastIndex = randomAccessFile.length()
    }
    return DownloadCall(
        file = file,
        rangeStart = lastIndex,
        onProcess = onProcess,
        call = responseCall({
            block.invoke(this)
            if (lastIndex > 0) {
                addHeader("Range", "bytes=$lastIndex-")
            }
        })
    )
}