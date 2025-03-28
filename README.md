# KtHttp 使用说明

## 概述

KtHttp 是一个基于 Kotlin 和 OkHttp 的轻量级 HTTP 客户端库 ，提供了简洁的 API 和多种响应处理方式（回调、协程、Flow）。

## 基本用法

### 1. HttpClient和Transform

```kotlin
object TestApi {
    var mHttpClient = HttpClient() //初始化httpClient,可以选择使用自己创建的OkhttpClient
    var gson = Gson()

    /**
     * 用于解析json数据，目前已实现了Gson解析  需要其他解析器可以自己实现Transform
     * 这里需要利用kotlin inline保留泛型类型
     */
    inline fun <reified T> jsonTransForm(): Transform<T>{
        return gson.transForm()
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
        // stringCall
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
     * 测试正常请求
     * requestOnMainThread在主线程回调
     */
    fun test(){
        testApi.test().requestOnMainThread({//异常处理
            tv_index.text = it.message
        }){ //处理数据
            tv_index.text = it.city
        }
    }

    /**
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
     * 协程2
     */
    fun testCoroutine2() {
        lifecycleScope.launch {
               //可不需要处理异常 如果出现异常数据返回为null
                val cityInfo = testApi.test().getResultSafe(){ //处理异常
                    tv_index.text = it.message
                }
                tv_index.text = cityInfo?.city

        }
    }

}

```


