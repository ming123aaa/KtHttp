package com.ohuang.kthttp.transform


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

inline fun <reified T> getGsonTypeToken(): TypeToken<T> {
    return object : TypeToken<T>() {}
}

inline fun <reified T> gsonTransForm(): Transform<T> {
    return getGsonTransForm(getGsonTypeToken<T>())
}

internal var ktHttp_mGson: Gson = Gson()

/**
 * 设置gson
 */
fun gsonTransFormSetGson(gson: Gson) {
    ktHttp_mGson = gson
}


fun <T> getGsonTransForm(typeToken: TypeToken<T>): Transform<T> {
    return GsonTransForm(ktHttp_mGson, typeToken.type)
}