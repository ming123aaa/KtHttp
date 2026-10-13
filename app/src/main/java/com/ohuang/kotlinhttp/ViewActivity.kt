package com.ohuang.kotlinhttp

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ohuang.kthttp.call.await
import com.ohuang.kthttp.call.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class ViewActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private lateinit var btnContainer: LinearLayout

    var count=0

    private fun log(text: String) {
        count++
        tvResult.text = "[$count]\n$text\n${tvResult.text}"
    }

    private fun addButton(label: String, action: suspend () -> Unit) {
        val btn = Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener {
                lifecycleScope.launch(Dispatchers.Main) {
                    log(">>> $label 开始")
                    try {
                        action()
                    } catch (e: Exception) {
                        log("异常: ${e.message}")
                    }
                }
            }
        }
        btnContainer.addView(btn)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        // === 按钮区域（上半部分，独立滚动） ===
        val btnScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        btnContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 16)
        }
        btnScroll.addView(btnContainer)

        // === 分隔线 ===
        val divider = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2
            )
            setBackgroundColor(Color.parseColor("#33000000"))
        }

        // === 日志区域（下半部分，独立滚动） ===
        val logScroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        tvResult = TextView(this).apply {
            text = "点击按钮开始请求 (公共测试服务器)\n"
            textSize = 13f
            setPadding(32, 16, 32, 32)
            gravity = Gravity.TOP or Gravity.START
        }
        logScroll.addView(tvResult)

        root.addView(btnScroll)
        root.addView(divider)
        root.addView(logScroll)
        setContentView(root)

        // --- 基础请求 ---
        addButton("jsonCall (GET jsonplaceholder)") {
            val post = testApi.getPost().await()
            log("Post id=${post.id} title=${post.title}")
        }

        addButton("stringCall (GET httpbin)") {
            val html = testApi.getHttpBin().await()
            log("响应长度: ${html.length}")
        }

        // --- POST 请求 ---
        addButton("POST 表单") {
            val result = testApi.postForm().await()
            log("POST 结果: 已收到回显 (${result.length} 字符)")
        }

        addButton("POST JSON") {
            val result = testApi.postJsonExample().await()
            log("JSON POST 结果: 已收到回显 (${result.length} 字符)")
        }

        // --- PUT / DELETE / PATCH ---
        addButton("PUT 请求") {
            val result = testApi.putExample().await()
            log("PUT id=${result.id} title=${result.title}")
        }

        addButton("DELETE 请求") {
            val result = testApi.deleteExample().await()
            log("DELETE 结果长度: ${result.length}")
        }

        addButton("PATCH 请求") {
            val result = testApi.patchExample().await()
            log("PATCH id=${result.id} title=${result.title}")
        }

        // --- 自定义状态码 ---
        addButton("validHttpCode (200-404) -> /status/404") {
            val result = testApi.customValidCode().await()
            log("200-404 放行，成功拿到数据 (${result.length} 字符)")
        }

        addButton("validHttpCode(null) 全部放行 -> /status/404") {
            val result = testApi.allCodeValid().await()
            log("全部放行，成功拿到数据 (${result.length} 字符)")
        }

        // --- HttpResponse 类型 ---
        addButton("jsonHttpResponseCall") {
            val resp = testApi.getPostWithResponse().await()
            log("code=${resp.raw().code} title=${resp.body?.title ?: "无"}")
        }

        addButton("stringHttpResponseCall") {
            val resp = testApi.getStringWithResponse().await()
            log("code=${resp.raw().code} 长度=${resp.body?.length ?: 0}")
        }

        // --- 文件信息 ---
        addButton("downloadFileInfo (/bytes/1024)") {
            val info = testApi.checkFileInfo("$HTTPBIN/bytes/1024").await()
            log("文件大小: ${info.contentLength.fileSize}  类型: ${info.contentType}")
        }

        addButton("downloadFileSize (/bytes/1024)") {
            val size = testApi.checkFileSize("$HTTPBIN/bytes/1024").await()
            log("文件大小: ${size.fileSize}")
        }

// --- 数据包装 ---
        addButton("HttpData<T> 包装层") {
            val post = testApi.getWrappedPost().await()
            log("包装结果: id=${post.id} title=${post.title}")
        }

        // --- kotlinx.serialization ---
        addButton("kotlinx.serialization 解析") {
            val post = testApi.getPostKx().await()
            log("kx: id=${post.id} title=${post.title}")
        }

        addButton("kotlinx.serialization (自定义Json)") {
            val post = testApi.getPostKxCustom().await()
            log("kx自定义: id=${post.id} title=${post.title}")
        }

        // --- HttpCall.map ---
        addButton("map 转换 (取 title)") {
            val title = testApi.getPost().map { it.title }.await()
            log("映射结果: $title")
        }

        // --- Java 互操作 ---
        addButton("MainHttpCall (Java 风格)") {
            val post = testApi.demoForJava().waitResult(5000)
            log("Java 风格结果: id=${post.id} title=${post.title}")
        }

        // --- Hook / OnError 等监听 ---
        addButton("Hook / 监听") {
            val result = testApi.withHooks().await()
            log("Hook 请求完成 (${result.length} 字符)")
        }

        // --- 请求构建回调 ---
        addButton("onRequestBuild / hookRequest") {
            val result = testApi.getUrlContent().await()
            log("请求构建回调完成 (${result.length} 字符)")
        }
    }

    companion object {
        private const val HTTPBIN = "https://httpbin.org"

        val Long.fileSize: String
            get() = when {
                this < 1024 -> "$this B"
                this < 1024 * 1024 -> String.format(Locale.US, "%.2f KB", this / 1024.0)
                this < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f MB", this / (1024.0 * 1024))
                this < 1024L * 1024 * 1024 * 1024 -> String.format(
                    Locale.US, "%.2f GB",
                    this / (1024.0 * 1024 * 1024)
                )
                else -> String.format(Locale.US, "%.2f TB", this / (1024.0 * 1024 * 1024 * 1024))
            }
    }
}