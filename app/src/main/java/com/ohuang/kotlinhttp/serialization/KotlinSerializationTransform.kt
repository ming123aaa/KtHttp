package com.ohuang.kotlinhttp.serialization

import com.ohuang.kthttp.transform.Transform
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * kotlinx.serialization 的 Transform 适配器
 */
class KotlinSerializationTransform<T>(
    private val json: Json,
    private val serializer: KSerializer<T>
) : Transform<T> {

    override fun transform(string: String): T {
        return json.decodeFromString(serializer, string)
    }
}

/** 便捷创建 [KotlinSerializationTransform]，使用默认 [Json] 配置 */
inline fun <reified T> defaultJsonTransform(): Transform<T> {
    return KotlinSerializationTransform(Json, kotlinx.serialization.serializer())
}

/** 使用自定义 [Json] 配置 */
inline fun <reified T> Json.transform(): Transform<T> {
    return KotlinSerializationTransform(this, kotlinx.serialization.serializer())
}