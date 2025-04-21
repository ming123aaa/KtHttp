package com.ohuang.kthttp.transform

interface Transform<T> {

    fun transform(string:String): T?
}


