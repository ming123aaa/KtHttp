package com.ohuang.kthttp

class KtHttpRequest : HttpRequest(),KtHttpConfig {
    internal var configs = HashMap<String, Any>()
    override fun setConfig(name: String, value: Any) {
        configs[name] = value
    }
}