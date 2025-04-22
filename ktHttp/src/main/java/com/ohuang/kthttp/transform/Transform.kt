package com.ohuang.kthttp.transform

/**
 * String类型转换器
 */
interface Transform<T> {

    fun transform(string:String): T?
}


