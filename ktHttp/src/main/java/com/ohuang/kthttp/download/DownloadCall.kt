package com.ohuang.kthttp.download

import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.KtHttpCall
import com.ohuang.kthttp.call.KtHttpException
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

open class DownloadCall(
    private var file: File,//下载的文件
    private var rangeStart: Long = 0,//断点下载开始点
    private var onProcess: (current: Long, total: Long) -> Unit,//下载进度
    call: HttpCall<Response>
) : KtHttpCall<File, Response>(call) {

    companion object {
        private const val BUFFER_SIZE = 8192L // 8KB 缓冲区
    }

    protected fun download(response: Response) {
        // 1. 检查响应是否成功
        if (!(response.code==200||response.code==206)) {
            throw KtHttpException("HTTP ${response.code}: ${response.message}")
        }

        // 2. 获取响应体和总大小
        val responseBody = response.body ?: throw KtHttpException("Response body is null")
        val totalSize = responseBody.contentLength() // 文件总大小
        // 断点续传时，实际需要下载的字节数
        val bytesToDownload = getTotalSize(response, totalSize)

        // 3. 准备文件和输出流 (使用 RandomAccessFile 支持断点续传)
        var randomAccessFile: RandomAccessFile? = null
        var fileChannel: FileChannel? = null
        try {
            // 以读写模式打开文件
            randomAccessFile = RandomAccessFile(file, "rw")
            fileChannel = randomAccessFile.channel

            // 4. 设置文件指针到断点位置
            if (rangeStart > 0&&response.code==206) {
                if (rangeStart>bytesToDownload){
                    throw KtHttpException("rangeStart > total size")
                }
                // 移动到断点位置，准备追加写入
                fileChannel.position(rangeStart)
            } else {

                // 如果不是断点续传，确保文件是空的（或覆盖）
                // 注意：如果 file 存在且非空，这里会覆盖原有内容。
                // 如果你想在非断点情况下追加，可以移除 truncate(0) 并确保 position 在末尾。
                // 通常下载新文件会覆盖。
                fileChannel.truncate(0) // 清空文件
                fileChannel.position(0)
            }

            // 5. 获取输入流并开始下载
            responseBody.byteStream().use { inputStream ->
                val buffer = ByteArray(BUFFER_SIZE.toInt())
                var bytesRead: Int
                var currentBytes = rangeStart // 当前已下载的总字节数（包含断点前的部分）

                // 6. 循环读取数据块并写入文件
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead)
                    fileChannel.write(byteBuffer)
                    currentBytes += bytesRead

                    // 7. 通知进度回调
                    // 注意：currentBytes 是包含断点前数据的总下载量
                    // totalSize 或 bytesToDownload 是总大小
                    onProcess(currentBytes, bytesToDownload)
                }
            }

            // 8. 下载完成，确保数据写入磁盘
            fileChannel.force(true)

            // 可选：验证下载完整性（例如检查文件大小是否等于 expectedSize）
            if (file.length() != bytesToDownload) {
                throw IOException("Download incomplete. Expected ${bytesToDownload} bytes, got ${file.length()}")
            }

        } catch (e: Exception) {
            // 下载过程中发生异常
            throw e
        } finally {
            // 9. 确保资源关闭
            try {
                fileChannel?.close()
            } catch (e: Exception) {
                // 忽略关闭异常
            }
            try {
                randomAccessFile?.close()
            } catch (e: Exception) {
                // 忽略关闭异常
            }
        }
    }

    private fun getTotalSize(response: Response, totalSize: Long): Long = if (rangeStart > 0) {
        // 如果 rangeStart > 0，表示是断点续传，总大小需要加上已下载的部分
        // 但 contentLength() 返回的是本次请求的数据量，不是文件总大小。
        // 这里需要根据实际情况处理。如果服务器支持 Range 请求并返回了 Content-Range，
        // 可以解析出文件总大小。这里简化处理，假设我们知道总大小或者用 contentLength() 作为剩余部分。
        // 更严谨的做法是解析 Content-Range header。
        val contentRange = response.header("Content-Range")
        if (contentRange != null && contentRange.startsWith("bytes ")) {
            // 格式如: bytes 206-499/500 或 bytes 206-499/*
            val parts = contentRange.substring(6).split("/")
            val rangePart = parts[0] // "206-499"
            val totalPart = parts.getOrNull(1) // "500" 或 "*"
            if (totalPart != null && totalPart != "*") {
                try {
                    totalPart.toLong() // 这是文件的总大小
                } catch (e: NumberFormatException) {
                    // 如果解析失败，还是用 contentLength
                    totalSize
                }
            } else {
                // 如果是 "*" 或者没有，我们只能知道剩余大小
                totalSize
            }
        } else {
            totalSize
        }
    } else {
        totalSize
    }

    override fun request(error: (Throwable) -> Unit, callback: (File) -> Unit) {
        call.request(error = error) { response ->
            download(response)
            callback.invoke(file)//下载完成
        }
    }

}