package com.ohuang.kthttp.transform

import com.ohuang.kthttp.Transform

object StringTransForm : Transform<String> {
    override fun transform(string: String): String? {
        return string
    }
}