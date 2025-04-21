package com.ohuang.kthttp

/**
 * 设置配置，可通过HttpCall.getConfigs()拿到
 */
interface KtHttpConfig {
    fun setConfig(name: String, value: Any)
}

class KtHttpConfigImpl : KtHttpConfig {
    var map = HashMap<String, Any>()
    override fun setConfig(name: String, value: Any) {
        map[name] = value
    }
}