package com.ohuang.kthttp.transform


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ohuang.kthttp.Transform
import java.lang.reflect.Type
/**
 * json解析
 */
class GsonTransForm<T>(var gson: Gson, var typeToken: Type) : Transform<T> {

    override fun transform(string: String): T? {
        return gson.fromJson<T>(
            string,
            typeToken
        )
    }
}

/**
 * json解析
 */
inline fun <reified T> Gson.transForm(
    typeToken: Type = object : TypeToken<T>() {}.type
): Transform<T> {
    return GsonTransForm(this, typeToken)
}