package com.ohuang.kthttp

/**
 * 设置配置，可通过HttpCall.getConfigs()拿到
 */
interface KtHttpConfig {
    fun setConfig(name: String, value: Any)

    fun getConfig(name: String): Any?
}

inline fun <reified T> KtHttpConfig.getConfigForType(name: String): T? {
    var config = getConfig(name)
    if (config is T){
        return config
    }
    return null
}

open class KtHttpConfigImpl(var configs: MutableMap<String, Any> = HashMap<String, Any>()) :
    KtHttpConfig {

    override fun setConfig(name: String, value: Any) {
        configs[name] = value
    }

    override fun getConfig(name: String): Any? {
        if (!configs.containsKey(name)) return null
        return configs[name]
    }
}