package com.ohuang.kthttp.util

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object UrlAddParams {
    private fun String.urlEncode(): String {
        try {
            return URLEncoder.encode(this, "utf-8")
        } catch (e: UnsupportedEncodingException) {
        }
        return this
    }
     fun urlAddParams(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url

        val baseUrl: String
        val queryString: String?
        val fragment: String?

        // Split URL into base, query and fragment parts
        val fragmentSplit = url.split("#", limit = 2)
        baseUrl = fragmentSplit[0]
        fragment = if (fragmentSplit.size > 1) "#" + fragmentSplit[1] else null

        val querySplit = baseUrl.split("?", limit = 2)
        val base = querySplit[0]
        queryString = if (querySplit.size > 1) querySplit[1] else null

        // Parse existing query parameters
        val existingParams = mutableMapOf<String, String>()
        queryString?.split("&")?.forEach { param ->
            param.split("=", limit = 2).let {
                if (it.size == 1) {
                    existingParams[it[0]] = ""
                } else {
                    existingParams[it[0]] = it[1]
                }
            }
        }

        // Combine existing and new parameters (new params overwrite existing ones)
        val combinedParams = existingParams.toMutableMap()
        combinedParams.putAll(params.mapValues { it.value.urlEncode() })

        // Build new query string
        val newQueryString = combinedParams.entries.joinToString("&") {
            "${it.key.urlEncode()}=${it.value}"
        }

        // Reconstruct the URL
        return buildString {
            append(base)
            if (newQueryString.isNotEmpty()) {
                append("?")
                append(newQueryString)
            }
            fragment?.let { append(it) }
        }
    }

}