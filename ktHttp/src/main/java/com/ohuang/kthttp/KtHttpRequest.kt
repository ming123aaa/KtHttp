package com.ohuang.kthttp

class KtHttpRequest(ktHttpConfigImpl:KtHttpConfigImpl) : HttpRequest(),KtHttpConfig {
    internal var configs = HashMap<String, Any>()
    init {
        configs.putAll(ktHttpConfigImpl.configs)
    }
    override fun setConfig(name: String, value: Any) {
        configs[name] = value
    }
}