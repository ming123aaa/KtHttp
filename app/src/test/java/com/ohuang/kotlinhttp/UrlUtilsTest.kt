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
    fun `urlAddParams should add params to URL without query`() {
        val url = "https://example.com"
        val params = mapOf("key1" to "value1", "key2" to "value2")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com?key1=value1&key2=value2", result)
    }

    @Test
    fun `urlAddParams should encode special characters`() {
        val url = "https://example.com"
        val params = mapOf("query" to "hello world", "user" to "张三")
        val result = urlAddParams(url, params)
        assertEquals("https://example.com?query=hello+world&user=%E5%BC%A0%E4%B8%89", result)
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
}