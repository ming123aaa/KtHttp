package com.ohuang.kthttp.util

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

object UrlAddParams {

    /**
     * URL 编码
     * @param isHandleSpaces 是否将空格编码为 %20（默认 true）
     */
    private fun String.urlEncode(isHandleSpaces: Boolean = true): String {
        return try {
            val encoded = URLEncoder.encode(this, "utf-8")
            if (isHandleSpaces) {
                encoded.replace("+", "%20")
            } else {
                encoded
            }
        } catch (e: UnsupportedEncodingException) {
            this
        }
    }

    /**
     * URL 解码
     * 注意：将 + 号也解码为空格（符合 application/x-www-form-urlencoded 规范）
     */
    private fun String.urlDecode(): String {
        return try {
            // 先将 + 替换为 %20，然后统一解码
            val normalized = this.replace("+", "%20")
            URLDecoder.decode(normalized, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            this
        }
    }

    /**
     * 解析 URL，返回基础路径、查询参数 Map 和 fragment
     */
    private fun parseUrl(url: String): Triple<String, MutableMap<String, String>, String?> {
        // 分离 fragment
        val fragmentSplit = url.split("#", limit = 2)
        val baseUrl = fragmentSplit[0]
        val fragment = if (fragmentSplit.size > 1) "#${fragmentSplit[1]}" else null

        // 分离查询参数
        val querySplit = baseUrl.split("?", limit = 2)
        val base = querySplit[0]
        val queryString = if (querySplit.size > 1) querySplit[1] else null

        // 解析现有查询参数
        val existingParams = mutableMapOf<String, String>()
        queryString?.split("&")?.forEach { param ->
            if (param.isNotEmpty()) {
                param.split("=", limit = 2).let {
                    val key = it[0].urlDecode()
                    val value = if (it.size == 1) "" else it[1].urlDecode()
                    existingParams[key] = value
                }
            }
        }

        return Triple(base, existingParams, fragment)
    }

    /**
     * 构建 URL 字符串
     */
    private fun buildUrl(base: String, params: Map<String, String>, fragment: String?): String {
        return buildString {
            append(base)
            if (params.isNotEmpty()) {
                append("?")
                append(params.entries.joinToString("&") { (key, value) ->
                    "${key.urlEncode()}=${value.urlEncode()}"
                })
            }
            fragment?.let { append(it) }
        }
    }

    /**
     * 获取 URL 中的所有查询参数
     */
    fun getUrlParams(url: String): MutableMap<String, String> {
        val (_, existingParams, _) = parseUrl(url)
        return existingParams
    }

    /**
     * 替换 URL 的所有参数（不保留原有参数）
     */
    fun urlReplaceParams(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url

        val (base, _, fragment) = parseUrl(url)
        return buildUrl(base, params, fragment)
    }

    /**
     * 添加/覆盖 URL 参数（保留原有参数）
     */
    fun urlAddParams(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url

        val (base, existingParams, fragment) = parseUrl(url)

        // 合并参数（新参数覆盖旧参数）
        val combinedParams = mutableMapOf<String, String>()
        combinedParams.putAll(existingParams)
        combinedParams.putAll(params)

        return buildUrl(base, combinedParams, fragment)
    }
}