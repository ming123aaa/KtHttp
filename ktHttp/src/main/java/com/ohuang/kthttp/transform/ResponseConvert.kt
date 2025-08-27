package com.ohuang.kthttp.transform

import okhttp3.Response

interface ResponseConvert<T> {
    fun convert(response: Response): T
}