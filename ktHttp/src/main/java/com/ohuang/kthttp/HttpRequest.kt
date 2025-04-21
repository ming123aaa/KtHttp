package com.ohuang.kthttp

import okhttp3.Request

class HttpRequest():KtHttpConfig {
    internal var builder = Request.Builder().get()
    var url: String = ""
    internal var configs =HashMap<String,Any>()

    /**
     * 设置配置，可通过HttpCall.getConfigs()拿到
     */
    override fun setConfig(name:String, value:Any){
        this.configs[name]=value
    }

    /**
     * 没提供的方法可以加
     * okhttp的Request.Builder
     */
    fun requestBuilderBlock(block: Request.Builder.() -> Unit) {
        block.invoke(builder)
    }

    /**
     * 请求的url
     * 想要加参数可以用 urlParams()
     * @param url
     */
    fun url(url: String) {
        this.url = url
    }


    fun addHeader(key: String, value: String) {
        builder.addHeader(key, value)
    }

    internal fun build(): Request {
        if (url.isEmpty()) {
            throw IllegalArgumentException("url is empty")
        }
        return builder.url(url).build()
    }

}

