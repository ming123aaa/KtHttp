package com.ohuang.kthttp

import okhttp3.Request
import java.util.Collections

class KtHttpRequest(ktHttpConfigImpl:KtHttpConfigImpl, builder:Request.Builder) : HttpRequest(builder),KtHttpConfig {
    internal var configs: MutableMap<String, Any> = Collections.synchronizedMap(HashMap<String, Any>())
    init {
        configs.putAll(ktHttpConfigImpl.configs)
    }
    override fun setConfig(name: String, value: Any) {
        configs[name] = value
    }

    override fun getConfig(name: String): Any? {
        return configs[name]
    }
}