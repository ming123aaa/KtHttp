package com.ohuang.kotlinhttp.data

import kotlinx.serialization.Serializable

@Serializable
data class PostKx(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)