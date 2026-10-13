package com.ohuang.kotlinhttp

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.ohuang.kotlinhttp.data.HttpData
import com.ohuang.kotlinhttp.data.Post
import com.ohuang.kotlinhttp.data.PostKx
import com.ohuang.kotlinhttp.serialization.defaultJsonTransform
import com.ohuang.kotlinhttp.serialization.transform
import com.ohuang.kotlinhttp.utli.UriToFile
import com.ohuang.kthttp.KtHttp
import com.ohuang.kthttp.KtHttpRequest
import com.ohuang.kthttp.addHeaders
import com.ohuang.kthttp.call.ErrorResponseException
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.KtHttpException
import com.ohuang.kthttp.call.map
import com.ohuang.kthttp.config.hookResponse
import com.ohuang.kthttp.config.hookStringBody
import com.ohuang.kthttp.config.onError
import com.ohuang.kthttp.config.onRequest
import com.ohuang.kthttp.config.onResponse
import com.ohuang.kthttp.config.onStringBody
import com.ohuang.kthttp.config.validHttpCode
import com.ohuang.kthttp.delete
import com.ohuang.kthttp.download
import com.ohuang.kthttp.download.FileInfo
import com.ohuang.kthttp.downloadFileInfo
import com.ohuang.kthttp.downloadFileSize
import com.ohuang.kthttp.jsonCall
import com.ohuang.kthttp.jsonHttpResponseCall
import com.ohuang.kthttp.patch
import com.ohuang.kthttp.post
import com.ohuang.kthttp.postJson
import com.ohuang.kthttp.put
import com.ohuang.kthttp.stringHttpResponseCall
import com.ohuang.kthttp.transform.Transform
import com.ohuang.kthttp.transform.transForm
import com.ohuang.kthttp.upload.addFile
import com.ohuang.kthttp.upload.addFileInputSteam
import com.ohuang.kthttp.upload.uploadFile
import com.ohuang.kthttp.url
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.File

// 公共测试服务器，用于演示
private const val HTTPBIN = "https://httpbin.org"
private const val JSONPLACEHOLDER = "https://jsonplaceholder.typicode.com"

object testApi {
    var gson = Gson()

    var mHttpClient = KtHttp(
        globalKtConfigCall = {
            onStringBody { body ->
                println("全局 onStringBody: $body")
            }
        },
        forceKtConfigCall = {
            onError { e, call, response ->
                e.printStackTrace()
                if (e is ErrorResponseException) {
                    println("errorResponse: ${e.errorResponse}")
                    println("errorBody: ${e.errorResponse.errorBodyString()}")
                }
                println("强制 onError: $e  url=${call.request().url}")
            }
        }
    )

    // ==================== 基础请求 ====================

    /** jsonCall — 使用 jsonplaceholder 公共 API */
    fun getPost(): HttpCall<Post> {
        return mHttpClient.jsonCall<Post> {
            url("$JSONPLACEHOLDER/posts/1")
        }
    }

    /** stringCall — 使用 httpbin 公共 API */
    fun getHttpBin(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/get") {
                addParam("name", "kthttp")
                addParam("version", "2.0")
            }
        }
    }

    /** responseCall — 获取原始 Response */
    fun getRawResponse(): HttpCall<Response> {
        return mHttpClient.responseCall {
            url("$HTTPBIN/get")
        }
    }

    // ==================== POST 请求 ====================

    /** POST 表单 — httpbin 会回显提交的数据 */
    fun postForm(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/post")
            post {
                addParam("username", "test")
                addParam("password", "123456")
            }
        }
    }

    /** POST JSON */
    fun postJsonExample(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/post")
            postJson {
                addParam("name", "kthttp")
                addParam("type", "json")
            }
        }
    }

    // ==================== PUT / DELETE / PATCH ====================

    fun putExample(): HttpCall<Post> {
        return mHttpClient.jsonCall<Post> {
            url("$JSONPLACEHOLDER/posts/1")
            put("""{"id":1,"title":"updated","body":"body","userId":1}""".toRequestBody("application/json".toMediaType()))
        }
    }

    fun deleteExample(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$JSONPLACEHOLDER/posts/1")
            delete()
        }
    }

    fun patchExample(): HttpCall<Post> {
        return mHttpClient.jsonCall<Post> {
            url("$JSONPLACEHOLDER/posts/1")
            patch("""{"title":"patched title"}""".toRequestBody("application/json".toMediaType()))
        }
    }

    // ==================== 请求配置 ====================

    /** Hook / 监听 示例 */
    fun withHooks(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/get")
            hookResponse {
                println("hookResponse: $it")
                return@hookResponse it
            }
            onResponse { println("onResponse: $it") }
            hookStringBody {
                println("hookStringBody: $it")
                return@hookStringBody it
            }
            onStringBody { println("onStringBody: $it") }
            onError { throwable -> println("onError: $throwable") }
            onRequest { call -> println("onRequest: $call") }
        }
    }

    // ==================== 自定义 HTTP 状态码 ====================

    /** httpbin/status/404 会返回 404 — 把 200-404 都视为成功 */
    fun customValidCode(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/status/404")
            validHttpCode { code -> code in 200..404 }
        }
    }

    /** 完全关闭状态码检查 — 404 也不会抛异常 */
    fun allCodeValid(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/status/404")
            validHttpCode(null)
        }
    }

    // ==================== 数据包装（HttpData<T>） ====================

    inline fun <reified T> jsonTransForm(): Transform<T> {
        return gson.transForm()
    }

    fun <T> HttpCall<HttpData<T>>.toData(): HttpCall<T> {
        return this.map {
            if (it.status!=200){
                throw KtHttpException("status is ${it.status},message=${it.message}")
            }
            if (it.data == null) {
                throw KtHttpException("data is null, message=${it.message}")
            }

            it.data!!
        }
    }

    inline fun <reified T> request(noinline block: KtHttpRequest.() -> Unit): HttpCall<T> {
        return mHttpClient.httpCall<HttpData<T>>(jsonTransForm(), block).toData()
    }

    fun getWrappedPost(): HttpCall<Post> {
        return request<Post> {
            hookStringBody { str ->
                return@hookStringBody """{
                    "status":200,
                    "message":"请求成功",
                    "data":$str
                    } """.trimMargin()
            }



            url("$JSONPLACEHOLDER/posts/1")
        }
    }

    // ==================== kotlinx.serialization 解析 ====================

    /** 使用 kotlinx.serialization 默认配置 */
    fun getPostKx(): HttpCall<PostKx> {
        return mHttpClient.httpCall(defaultJsonTransform()) {
            url("$JSONPLACEHOLDER/posts/1")
        }
    }

    /** 使用自定义 Json 配置（忽略未知字段、宽松模式等） */
    fun getPostKxCustom(): HttpCall<PostKx> {
        val customJson = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        return mHttpClient.httpCall(customJson.transform()) {
            url("$JSONPLACEHOLDER/posts/1")
        }
    }

    // ==================== HttpResponse 类型 ====================

    /** 获取带状态码/头的完整响应（不自动抛异常） */
    fun getPostWithResponse(): HttpCall<com.ohuang.kthttp.call.HttpResponse<Post>> {
        return mHttpClient.jsonHttpResponseCall<Post> {
            url("$JSONPLACEHOLDER/posts/1")
        }
    }

    fun getStringWithResponse(): HttpCall<com.ohuang.kthttp.call.HttpResponse<String>> {
        return mHttpClient.stringHttpResponseCall {
            url("$HTTPBIN/get")
        }
    }

    // ==================== 文件下载 ====================

    /** httpbin/bytes/1024 返回 1024 字节随机数据 */
    fun downloadFile(file: File, onProcess: (current: Long, total: Long) -> Unit): HttpCall<File> {
        return mHttpClient.download(file, isContinueDownload = true, onProcess = onProcess) {
            url("$HTTPBIN/bytes/1024")
        }
    }

    fun checkFileInfo(url: String): HttpCall<FileInfo> {
        return mHttpClient.downloadFileInfo {
            url(url)
        }
    }

    fun checkFileSize(url: String): HttpCall<Long> {
        return mHttpClient.downloadFileSize {
            url(url)
        }
    }

    // ==================== 文件上传 ====================

    /** httpbin/post 接受 multipart 上传并回显 */
    fun uploadSingleFile(
        file: File,
        callBack: (current: Long, totalSize: Long) -> Unit
    ): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/post")
            uploadFile {
                addFile(key = "file", file = file, callBack = callBack)
            }
        }
    }

    fun uploadFromUri(
        context: Context,
        uri: Uri,
        callBack: (current: Long, totalSize: Long) -> Unit
    ): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/post")
            uploadFile {
                addFileInputSteam(
                    key = "file",
                    file = UriToFile.uriToFileInputStream(uri, context)!!,
                    fileName = UriToFile.getFileName(uri, context)!!,
                    callBack = callBack
                )
            }
        }
    }

    fun uploadMultipleFiles(
        files: List<File>,
        callBack: (current: Long, totalSize: Long, index: Int) -> Unit
    ): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/post")
            uploadFile {
                files.forEachIndexed { index, f ->
                    addFile(key = "file$index", file = f, callBack = { current, total ->
                        callBack(current, total, index)
                    })
                }
            }
        }
    }

    // ==================== 请求构建/监听完整示例 ====================

    fun getUrlContent(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("$HTTPBIN/get")
            onRequestBuild { request -> println("onRequestBuild: $request") }
            onRequestBuild { request -> println("onRequestBuild2: $request") }
            hookRequestBuild { request ->
                println("hookRequestBuild: $request")
                return@hookRequestBuild request
            }
            onRequest { request -> println("onRequest: $request") }
            onError { throwable -> println("onError: $throwable") }
            onStringBody { body -> println("onStringBody: $body") }
        }
    }

    // ==================== Java 互操作 ====================

    fun demoForJava(): MainHttpCall<Post> {
        return mHttpClient.jsonCall<Post> {
            url("$JSONPLACEHOLDER/posts/1")
            addHeaders {
                addParam("token", "demo-token")
            }
        }.toMainHttpCall()
    }
}