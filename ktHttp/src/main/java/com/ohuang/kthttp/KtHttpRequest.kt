package com.ohuang.kthttp

import okhttp3.Request

class KtHttpRequest(internal val ktHttpConfigImpl:KtHttpConfigImpl,builder:Request.Builder) : HttpRequest(builder),KtHttpConfig {
    internal var configs = HashMap<String, Any>()
    init {
        configs.putAll(ktHttpConfigImpl.configs)
    }
    override fun setConfig(name: String, value: Any) {
        configs[name] = value
    }
}