package com.ohuang.kthttp

interface KtHttpConfig {
    fun setConfigs(name: String, value: Any)
}

class KtHttpConfigImpl : KtHttpConfig {
    var map = HashMap<String, Any>()
    override fun setConfigs(name: String, value: Any) {
        map[name] = value
    }
}