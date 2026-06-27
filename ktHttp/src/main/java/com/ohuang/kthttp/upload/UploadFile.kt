package com.ohuang.kthttp.upload

import com.ohuang.kthttp.HttpRequest
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody


import java.io.File
import java.io.FileInputStream


/**
 *  使用MultipartBody
 */
fun HttpRequest.postMultipartBody(type: MediaType=MultipartBody.FORM,bodybuilder: MultipartBody.Builder.() -> Unit) {
    val requestBodyBuild = MultipartBody.Builder()
        .setType(type)
    bodybuilder.invoke(requestBodyBuild)
    builder.post(requestBodyBuild.build())
}

/**
 *  使用MultipartBody,配合[addFile]可进行文件上传
 */
fun HttpRequest.postUploadFile(type: MediaType=MultipartBody.FORM,bodybuilder: MultipartBody.Builder.() -> Unit) {
    uploadFile(type=type,bodybuilder=bodybuilder)
}

/**
 *  使用MultipartBody,配合[addFile]可进行文件上传
 */
fun HttpRequest.uploadFile(type: MediaType=MultipartBody.FORM,bodybuilder: MultipartBody.Builder.() -> Unit) {
    postMultipartBody(type = type,bodybuilder=bodybuilder)
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


/**
 * 上传文件
 */
fun MultipartBody.Builder.addFileInputSteam(
    key: String,
    file: FileInputStream,
    fileName: String ,
    mediaType: MediaType? = "application/octet-stream".toMediaTypeOrNull(),
    callBack: (current: Long, totalSize: Long) -> Unit = { _, _ -> }
) {
    addFormDataPart(
        key,
        fileName,
        FileInputStreamRequestBody(
            contentType = mediaType,
            file = file,
            callBack = callBack
        )
    )
}
