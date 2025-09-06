package com.ohuang.kthttp.upload

import com.ohuang.kthttp.HttpRequest
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody


import java.io.File


/**
 *  使用MultipartBody
 */
fun HttpRequest.postMultipartBody(bodybuilder: MultipartBody.Builder.() -> Unit) {
    var requestBodyBuild = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
    bodybuilder.invoke(requestBodyBuild)
    builder.post(requestBodyBuild.build())
}

/**
 *  使用MultipartBody,配合[addFile]可进行文件上传
 */
fun HttpRequest.postUploadFile(bodybuilder: MultipartBody.Builder.() -> Unit) {
    postMultipartBody(bodybuilder)
}

/**
 * 上传文件
 */
fun MultipartBody.Builder.addFile(
    key: String,
    file: File,
    fileName: String = file.name,
    mediaType: MediaType? = "application/octet-stream".toMediaTypeOrNull(),
    callBack: (current: Long, totalSize: Long) -> Unit = { _, _ -> }
) {
    addFormDataPart(
        key,
        fileName,
        ProgressRequestBody(
            contentType = mediaType,
            file = file,
            callBack = callBack
        )
    )
}