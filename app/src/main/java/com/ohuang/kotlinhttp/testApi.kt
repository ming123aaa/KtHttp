package com.ohuang.kotlinhttp

import com.google.gson.Gson
import com.ohuang.kotlinhttp.data.CityInfo
import com.ohuang.kotlinhttp.data.HttpData
import com.ohuang.kthttp.HttpClient
import com.ohuang.kthttp.HttpRequest
import com.ohuang.kthttp.KtHttpRequest
import com.ohuang.kthttp.transform.Transform
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.hookResponse
import com.ohuang.kthttp.call.hookStringBody
import com.ohuang.kthttp.call.onResponse
import com.ohuang.kthttp.call.onStringBody
import com.ohuang.kthttp.call.map
import com.ohuang.kthttp.call.onError
import com.ohuang.kthttp.post

import com.ohuang.kthttp.transform.transForm
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
        onError { e,r->
            println("强制onError:e=$e   url=${r?.request?.url}") }
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

            url("http://192.168.2.103:8080/main/files/test.json")
            hookResponse{ //可修改Response
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

            onError{  //出现错误回调
                println("onError:$it")
            }


        }
    }


    fun test2(): HttpCall<HttpData<CityInfo>> {
        //使用 httpCall 返回对象
        return mHttpClient.httpCall<HttpData<CityInfo>>(jsonTransForm()) {
            url("http://192.168.2.103:8080/main/files/test.json")
        }
    }

    fun test3(): MainHttpCall<CityInfo> {
        return request<CityInfo>() {
            url("http://192.168.2.103:8080/main/files/test.json")
        }.toMainHttpCall()
    }


    fun getFileHtml(): HttpCall<String> {
        /**
         * 使用 stringCall 返回可处理 字符串数据的Call
         */
        return mHttpClient.stringCall {
            urlParams("http://192.168.2.103:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")
            }
        }
    }

    fun getFileHtml2(): HttpCall<Response> {
        /**
         * 使用 responseCall 返回可处理 okhttp的response的Call
         */
        return mHttpClient.responseCall {
            urlParams("http://192.168.2.103:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")
            }
        }
    }

    fun getFileHtml3(): Call {
        /**
         * 使用 newCall 返回okhttp的Call
         */
        return mHttpClient.newCall {
            urlParams("http://192.168.2.103:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")
            }
        }
    }


    fun postFile(): HttpCall<String> {
        return mHttpClient.stringCall {
            url("http://192.168.2.103:8080/main/index")
            post() {
                addParam("path", "/base.apk.cache")
            }
            addHeader("Content-Type", "application/x-www-form-urlencoded")//支持添加头
            requestBuilderBlock { //需要其他功能可以直接使用okhttp的RequestBuilder
                this.header("Content-Type", "application/x-www-form-urlencoded")
            }

        }
    }

    fun download(file: File): FileHttpCall {
        return mHttpClient.responseCall {
            urlParams("http://192.168.2.103:8080/main/files/WebViewGoogle.apk") {
            }
        }.toFileCall(file)
    }


}