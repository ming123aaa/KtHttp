package com.ohuang.kthttp

/**
 * 设置配置，可通过HttpCall.getConfigs()拿到
 */
interface KtHttpConfig {
    fun setConfig(name: String, value: Any)
}

open class KtHttpConfigImpl(var configs: MutableMap<String, Any> = HashMap<String, Any>()) :
    KtHttpConfig {

    override fun setConfig(name: String, value: Any) {
        configs[name] = value
    }
}