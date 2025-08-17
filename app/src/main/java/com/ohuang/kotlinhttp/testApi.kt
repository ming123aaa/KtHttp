package com.ohuang.kotlinhttp

import com.google.gson.Gson
import com.ohuang.kotlinhttp.data.CityInfo
import com.ohuang.kotlinhttp.data.HttpData
import com.ohuang.kthttp.HttpClient
import com.ohuang.kthttp.KtHttpRequest
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.map
import com.ohuang.kthttp.call.toSafeTransformCall
import com.ohuang.kthttp.call.toStringHttpCallSafe
import com.ohuang.kthttp.config.hookResponse
import com.ohuang.kthttp.config.hookStringBody
import com.ohuang.kthttp.config.onError
import com.ohuang.kthttp.config.onResponse
import com.ohuang.kthttp.config.onStringBody
import com.ohuang.kthttp.download
import com.ohuang.kthttp.download.DownloadCall
import com.ohuang.kthttp.post
import com.ohuang.kthttp.postJson
import com.ohuang.kthttp.stringCallCode200
import com.ohuang.kthttp.stringCallNotCheck
import com.ohuang.kthttp.stringCallSafe
import com.ohuang.kthttp.transform.Transform
import com.ohuang.kthttp.transform.transForm
import com.ohuang.kthttp.upload.addFile
import com.ohuang.kthttp.upload.postMultipartBody
import com.ohuang.kthttp.upload.postUploadFile
import com.ohuang.kthttp.urlParams
import okhttp3.Call
import okhttp3.Response
import java.io.File

object testApi {
    var mHttpClient = HttpClient(globalKtConfigCall = {
        onStringBody {
            println("全局onStringBody:$it")
        }

    }, forceKtConfigCall = {
        onError { e, call, r ->
            println("强制onError:e=$e   url=${call.request().url}")
        }
    })
    var gson = Gson()

    /**
     * 用于解析json数据，目前已实现了Gson解析  需要其他解析器可以自己实现Transform
     * 这里需要利用kotlin inline保留泛型类型
     */
    inline fun <reified T> jsonTransForm(): Transform<T> {
        return gson.transForm()
    }

    /**
     * 请求封装的数据
     * 这里需要利用kotlin inline保留泛型类型
     */
    inline fun <reified T> request(noinline block: KtHttpRequest.() -> Unit): HttpCall<T> {
//        val jsonTransForm = gson.transForm<HttpData<T>>(object : TypeToken<HttpData<T>>() {}.type) //为了解决  kotlin版本小于1.8.20 可能会导致泛型丢失变成Data<Any>
        return mHttpClient.httpCall<HttpData<T>>(jsonTransForm(), block)
            .map {
                if (it.data == null) {
                    throw Exception(it.message)
                }
                return@map it.data!!
            }
    }

    /**
     * 获取封装的数据
     */
    fun test(): HttpCall<CityInfo> {
        return request<CityInfo>() {
            url("http://192.168.2.100:8080/main/files/test.json")

            urlParams(url) {
                addParam("aaa", "1111")
            }
            urlParams(url) {
                addParam("aaa", "2222")
            }
            hookResponse { //可修改Response
                println("hookResponse$it")
                return@hookResponse it
            }
            onResponse { //回调Response
                println("showResponse$it")
            }
            hookStringBody { //可修改字符串
                println("hookStringBody:$it")
                return@hookStringBody it
            }
            onError {  //出现错误回调
                println("onError:$it")
            }
        }
    }


    fun test2(): HttpCall<HttpData<CityInfo>> {
        //使用 httpCall 返回对象
        return mHttpClient.httpCall<HttpData<CityInfo>>(jsonTransForm()) {
            url("http://192.168.2.100:8080/main/files/test.json")
        }
    }

    fun test3(): MainHttpCall<CityInfo> {
        return request<CityInfo>() {
            url("http://192.168.2.100:8080/main/files/test.json")
        }.toMainHttpCall()
    }


    fun getFileHtml(): HttpCall<String> {
        /**
         * 使用 stringCall 返回可处理 字符串数据的Call
         */
        return mHttpClient.stringCall {
            urlParams("http://192.168.2.100:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")


            }
        }
    }

    fun getFileHtml2(): HttpCall<Response> {
        /**
         * 使用 responseCall 返回可处理 okhttp的response的Call
         */
        return mHttpClient.responseCall {
            urlParams("http://192.168.2.100:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")
            }
        }
    }

    fun getFileHtml3(): Call {
        /**
         * 使用 newCall 返回okhttp的Call
         */
        return mHttpClient.newCall {
            urlParams("http://192.168.2.100:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")
            }
        }
    }


    fun postFile(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("http://192.168.2.100:8080/main/index")
            post() {
                addParam("path", "/base.apk.cache")
            }

            addHeader("Content-Type", "application/x-www-form-urlencoded")//支持添加头
            requestBuilderBlock { //需要其他功能可以直接使用okhttp的RequestBuilder
                this.header("Content-Type", "application/x-www-form-urlencoded")
            }

        }
    }

    fun download(file: File, onProcess: (current: Long, total: Long) -> Unit): DownloadCall {
        return mHttpClient.download(file, isContinueDownload = true, onProcess = onProcess) {
            urlParams("http://192.168.2.123:8080/main/files/base.apk") {
            }
        }
    }

    fun uploadFile(file: File,callBack:(current: Long, totalSize: Long) -> Unit ): HttpCall<String> {
        return mHttpClient.stringCall {
            url("http://192.168.2.123:8080/main/fileUpload")
            postMultipartBody {//上传文件  postUploadFile or postMultipartBody
                addFile(key = "fileName", file = file, callBack = callBack)
            }
        }
    }

    fun uploadFiles(file: List<File>,callBack:(current: Long, totalSize: Long,index:Int) -> Unit ): HttpCall<String> {
        return mHttpClient.stringCall {
            url("http://192.168.2.123:8080/main/multifileUpload")
            postUploadFile {//上传文件  postUploadFile or postMultipartBody
                file.forEachIndexed { index,f-> //上传多个文件
                    addFile(key = "fileName",file = f, callBack = { current,total->
                        callBack(current,total,index)
                    })
                }

            }
        }
    }

    fun getUrlContent(url: String): HttpCall<String>{

        return mHttpClient.stringCallCode200 {
            url(url)

        }
    }





}