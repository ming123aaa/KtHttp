package com.ohuang.kthttp

import java.util.Collections

/**
 * 设置配置，可通过HttpCall.getConfigs()拿到
 */
interface KtHttpConfig {
    fun setConfig(name: String, value: Any)

    fun getConfig(name: String): Any?
}

inline fun <reified T> KtHttpConfig.getConfigForType(name: String): T? {
    val config = getConfig(name)
    if (config is T){
        return config
    }
    return null
}

open class KtHttpConfigImpl(var configs: MutableMap<String, Any> = Collections.synchronizedMap(HashMap<String, Any>())) :
    KtHttpConfig {

    override fun setConfig(name: String, value: Any) {
        configs[name] = value
    }

    override fun getConfig(name: String): Any? {
        return configs[name]
    }
}