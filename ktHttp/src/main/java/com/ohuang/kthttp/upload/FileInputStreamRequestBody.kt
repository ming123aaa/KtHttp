package com.ohuang.kthttp.upload


import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import java.io.FileInputStream
import java.io.IOException


class FileInputStreamRequestBody(
    val contentType: MediaType?,
    val file: FileInputStream,
    val contentSize: Long = file.channel.size(),
    val callBack: (current: Long, totalSize: Long) -> Unit
) : RequestBody() {

    val DEFAULT_BUFFER_SIZE: Long = 2048
    override fun contentType(): MediaType? {
        return contentType
    }

    override fun contentLength(): Long {
        return contentSize
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {

        file.source().use { source ->
            val buffer: Buffer = Buffer()
            var uploaded: Long = 0
            val fileSize: Long = contentSize
            var readCount: Long
            while ((source.read(buffer, DEFAULT_BUFFER_SIZE).also { readCount = it }) != -1L) {
                sink.write(buffer, readCount)
                uploaded += readCount
                callBack.invoke(uploaded, fileSize)
            }
        }
    }
}