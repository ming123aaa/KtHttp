package com.ohuang.kthttp

interface Transform<T> {

    fun transform(string:String): T?
}


