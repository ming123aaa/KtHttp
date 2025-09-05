# KtHttp 使用说明

## 概述

KtHttp 是一个基于 Kotlin 和 OkHttp 的轻量级 HTTP 客户端库 ，提供了简洁的 API 和多种响应处理方式（回调、协程、Flow）。

## 依赖

```groovy
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

```groovy
	dependencies {
	        implementation 'com.github.ming123aaa:KtHttp:1.2.3'
	}
```

## 基本用法

### 1. HttpClient和Transform 以及基本的请求

```kotlin
object TestApi {
    var mHttpClient = HttpClient() //初始化httpClient,可以选择使用自己创建的OkhttpClient
    var gson = Gson() //可使用内敛函数gson.transForm()来获取Transform 完成json数据解析


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
     * 用于解析json数据，目前已实现了Gson解析  需要其他解析器可以自己实现Transform
     * 这里需要利用kotlin inline保留泛型类型
     */
    inline fun <reified T> jsonTransForm(): Transform<T> {
        return gson.transForm()
    }

      /**
     * 获取封装的数据
     */
    fun test(): HttpCall<CityInfo> {
        return request<CityInfo>() {
            url("http://192.168.2.100:8080/main/files/test.json")
        }
    }
    
    fun test1(): HttpCall<HttpData<CityInfo>> {
        //使用jsonCall 请求json数据
        return mHttpClient.jsonCall<HttpData<CityInfo>> {
            url("http://192.168.2.67:8080/main/files/test.json")
        }
    }

    fun test2(): HttpCall<HttpData<CityInfo>> {
        //使用httpCall 可生成对象   需要传入Transform将字符串转成对象
        return mHttpClient.httpCall<HttpData<CityInfo>>(jsonTransForm()) {
            url("http://192.168.2.67:8080/main/files/test.json")
        }
    }
   

    fun getFileHtml(): HttpCall<String> {
        /**
         * 使用 stringCall 返回可处理 字符串数据的Call
         */
        return mHttpClient.stringCall {
            urlParams("http://192.168.2.67:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")
            }
        }
    }
    fun getFileHtml2(): HttpCall<Response> {
        /**
         * 使用 responseCall 返回可处理 okhttp的response的Call
         */
        return mHttpClient.responseCall {
            urlParams("http://192.168.2.67:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")
            }
        }
    }

    fun getFileHtml3(): Call {
        /**
         * 使用 newCall 返回okhttp的Call
         */
        return mHttpClient.newCall {
            urlParams("http://192.168.2.67:8080/main/index") { //给url添加参数
                addParam("path", "/base.apk.cache")
            }
        }
    }

    fun postFile(): HttpCall<String> {
        // stringCall 请求string
        return mHttpClient.stringCall {
            url("http://192.168.2.67:8080/main/index")
            post() { //post请求 目前仅支持get,post请求,需要其他请求通过 requestBuilderBlock{}里面自己实现
                addParam("path", "/base.apk.cache")
            }
            addHeader("Content-Type", "application/x-www-form-urlencoded")//支持添加头部
            requestBuilderBlock { //需要其他功能可自己实现，可以直接使用okhttp的RequestBuilder
                this.header("Content-Type", "application/x-www-form-urlencoded")
            }

        }
    }

    fun download(file: File, onProcess: (current: Long, total: Long) -> Unit): DownloadCall {
        //文件下载
        return mHttpClient.download(file, isContinueDownload = true, onProcess = onProcess) {
            urlParams("http://192.168.2.93:8080/main/files/%E9%93%B8%E4%BB%99/4399.apk") {
            }
        }
    }

    fun uploadFile(file: File,callBack:(current: Long, totalSize: Long) -> Unit ): HttpCall<String> {
        return mHttpClient.stringCall {
            url("http://192.168.2.123:8080/main/fileUpload")
            postMultipartBody { //上传文件  postUploadFile or postMultipartBody
                addFile(key = "fileName",file = file, callBack = callBack)
            }
        }
    }
    
}
```
### 2.HttpCall
HttpCall 提供了以下方法,调用request才会进行真正的请求
```kotlin
interface HttpCall<T> {
    fun request(error: (Throwable) -> Unit={}, callback: (T) -> Unit)

    fun cancel()

    fun isCancelled(): Boolean

    fun isExecuted():Boolean
}
```
HttpCall除了可以使用request进行请求外，支持通过flow或者kotlin协程来使用。
```kotlin
class ViewActivity : AppCompatActivity() {
    val tv_index: TextView by lazy {
        findViewById<TextView>(R.id.tv_index)
    }
    
    /**
     * request（）请求网络
     * 
     */
    fun test(){
        testApi.test().request({//异常处理
            tv_index.text = it.message
        }){ //处理数据
            tv_index.text = it.city
        }
    }

    /**
     * asFlow()可转成flow处理
     * flow
     */
  fun testFlow(){
        lifecycleScope.launch {
            testApi.test().asFlow().catch {
                //异常处理
                tv_index.text = it.message
            }.collect{
                //成功处理
                tv_index.text = it.city
            }
        }
  }

    /**
     * getResult（）通过协程请求并返回结果
     * 协程处理
     */
    fun testCoroutine() {
        lifecycleScope.launch {
            try {
                val cityInfo = testApi.test().getResult()
                tv_index.text = cityInfo.city
            } catch (e: Exception) {
                tv_index.text = e.message
            }
        }
    }

    /**
     * getResultSafe（）通过协程请求并返回结果,自动对异常捕获
     * 协程2
     */
    fun testCoroutine2() {
        lifecycleScope.launch {
               //可不需要处理异常 如果出现异常数据返回为null
                val cityInfo = testApi.test().getResultOrNull(){ //处理异常
                    tv_index.text = it.message
                }
                tv_index.text = cityInfo?.city

        }
    }

}
```

## 3.KtHttpConfig  
通过KtHttpConfig和HttpCall配合可实现一些额外的功能,以下是一些已实现的功能
```kotlin

var mHttpClient = HttpClient(globalKtConfigCall = {//全局配置,会被请求覆盖
    onStringBody { //回调字符串
        println("全局onStringBody:$it")
    }
}, forceKtConfigCall = {
    onError { error,call,respose-> //出现错误回调
        println("强制onError:e=$e   url=${call.request().url}") }
})
    /**
     *   一些其他的方法
     */
    fun test(): HttpCall<String> {
        return mHttpClient.stringCall {
            post()
            url("http://192.168.2.83:8080/main/files/test.json")
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

```

### 异常处理
```kotlin
   fun testCoroutine() {
    lifecycleScope.launch {
        try {
            val cityInfo = testApi.test().getResult()
        } catch (e: Exception) {
            if(e is ErrorResponseException){
               var errorResponse=e.errorResponse //可以获取错误响应
            }
            e.printStackTrace()
        }
    }
}
        

```



