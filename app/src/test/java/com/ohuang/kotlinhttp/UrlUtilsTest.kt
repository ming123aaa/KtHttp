package com.ohuang.kotlinhttp
import com.ohuang.kthttp.util.UrlAddParams.urlAddParams
import junit.framework.TestCase.assertEquals
import org.junit.Test


class UrlUtilsTest {

    @Test
    fun `urlAddParams should handle empty params`() {
        val url = "https://example.com"
        val result = urlAddParams(url, emptyMap())
        assertEquals(url, result)
    }

    @Test
    fun `urlAddParams should encode special characters`() {
        val url = "https://example.com"
        val params = mapOf("query" to "hello world", "user" to "张三")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com?query=hello%20world&user=%E5%BC%A0%E4%B8%89", result)
    }

    @Test
    fun `urlAddParams should encode special characters3`() {
        val url = "https://example.com?query=hello+world&%E5%BC%A0%E4%B8%89=user"
        val params = mapOf("query" to "hello world", "张三" to "user")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com?query=hello%20world&%E5%BC%A0%E4%B8%89=user", result)
    }

    @Test
    fun `urlAddParams should append params to existing query`() {
        val url = "https://example.com?existing=param"
        val params = mapOf("new" to "value")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com?existing=param&new=value", result)
    }

    @Test
    fun `urlAddParams should override existing params`() {
        val url = "https://example.com?key=old"
        val params = mapOf("key" to "new")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com?key=new", result)
    }

    @Test
    fun `urlAddParams should handle URL with fragment`() {
        val url = "https://example.com/path#section1"
        val params = mapOf("param" to "value")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com/path?param=value#section1", result)
    }

    @Test
    fun `urlAddParams should handle URL with query and fragment`() {
        val url = "https://example.com/path?q=test#section1"
        val params = mapOf("page" to "2")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com/path?q=test&page=2#section1", result)
    }

    @Test
    fun `urlAddParams should handle empty values`() {
        val url = "https://example.com"
        val params = mapOf("empty" to "", "flag" to "")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com?empty=&flag=", result)
    }


    @Test
    fun `urlAddParams should handle spaces in both key and value with 20 encoding`() {
        val url = "https://example.com"
        val params = mapOf("my query" to "hello world", "test param" to "value with spaces")
        val result = urlAddParams(url, params)
        // 使用 %20 编码空格
        assertEquals("https://example.com?my%20query=hello%20world&test%20param=value%20with%20spaces", result)
    }

    @Test
    fun `urlAddParams should handle leading and trailing spaces with 20 encoding`() {
        val url = "https://example.com"
        val params = mapOf(" key " to " value ", "trim" to "  test  ")
        val result = urlAddParams(url, params)
        // 前后空格使用 %20 编码
        assertEquals("https://example.com?%20key%20=%20value%20&trim=%20%20test%20%20", result)
    }

    @Test
    fun `urlAddParams should handle multiple consecutive spaces with 20 encoding`() {
        val url = "https://example.com"
        val params = mapOf("multiple   spaces" to "hello    world")
        val result = urlAddParams(url, params)
        // 多个连续空格使用 %20 编码
        assertEquals("https://example.com?multiple%20%20%20spaces=hello%20%20%20%20world", result)
    }



    @Test
    fun `urlAddParams should convert plus to 20 when overriding existing params`() {
        val url = "https://example.com?key=old+value"
        val params = mapOf("key" to "new value with spaces")
        val result = urlAddParams(url, params)
        // 覆盖时应该统一使用 %20 编码
        assertEquals("https://example.com?key=new%20value%20with%20spaces", result)
    }

    @Test
    fun `urlAddParams should handle spaces in fragment with 20 encoding`() {
        val url = "https://example.com#section with spaces"
        val params = mapOf("param" to "value")
        val result = urlAddParams(url, params)
        // fragment 中的空格通常保持不变
        assertEquals("https://example.com?param=value#section with spaces", result)
    }

    @Test
    fun `urlAddParams should handle spaces in both query and new params with consistent encoding`() {
        val url = "https://example.com?existing=hello%20world"
        val params = mapOf("new param" to "test value")
        val result = urlAddParams(url, params)
        // 都使用 %20 编码
        assertEquals("https://example.com?existing=hello%20world&new%20param=test%20value", result)
    }

    @Test
    fun `urlAddParams should normalize existing plus to 20 when merging`() {
        val url = "https://example.com?q=hello+world&another=test%20case"
        val params = mapOf("q" to "hello world", "extra" to "new param")
        val result = urlAddParams(url, params)
        // 覆盖时，旧的 + 应该被新的 %20 替换
        assertEquals("https://example.com?q=hello%20world&another=test%20case&extra=new%20param", result)
    }

    @Test
    fun `urlAddParams should handle empty string with spaces`() {
        val url = "https://example.com"
        val params = mapOf("empty" to "   ", "spaces" to "   ")
        val result = urlAddParams(url, params)
        // 只有空格的字符串应该被编码为 %20%20%20
        assertEquals("https://example.com?empty=%20%20%20&spaces=%20%20%20", result)
    }


}