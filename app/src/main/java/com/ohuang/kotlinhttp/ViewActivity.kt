package com.ohuang.kotlinhttp

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ohuang.kotlinhttp.R
import com.ohuang.kthttp.call.HttpCall
import com.ohuang.kthttp.call.asFlow
import com.ohuang.kthttp.call.await
import com.ohuang.kthttp.call.getResult
import com.ohuang.kthttp.call.getResultOrNull
import com.ohuang.kthttp.download.DownloadCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import java.util.Formatter


class ViewActivity : AppCompatActivity() {
    private val TAG = "ViewActivity"
    var lastEvent: MotionEvent? = null

    val tv_index: TextView by lazy {
        findViewById<TextView>(R.id.tv_index)
    }
    val tv_button: TextView by lazy {
        findViewById<TextView>(R.id.tv_button)
    }
    var stateFlow: MutableStateFlow<String> = MutableStateFlow("没数据")

    val Long.fileSize: String
        get() = when {
            this < 1024 -> "$this B"
            this < 1024 * 1024 -> String.format("%.2f KB", this / 1024.0)
            this < 1024 * 1024 * 1024 -> String.format("%.2f MB", this / (1024.0 * 1024))
            this < 1024L * 1024 * 1024 * 1024 -> String.format("%.2f GB", this / (1024.0 * 1024 * 1024))
            else -> String.format("%.2f TB", this / (1024.0 * 1024 * 1024 * 1024))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        tv_button.setOnClickListener {
            lifecycleScope.launch {
                val filesize=testApi.checkFileSize("http://192.168.2.138:8080/main/files/testAssets/config.json").await()
                stateFlow.emit(filesize.fileSize)
            }

        }

        lifecycleScope.launch {
            stateFlow.collect {
                tv_index.text = it
            }
        }
    }

    /**
     * 测试正常请求
     * requestOnMainThread可在主线程请求
     */
    fun test() {
        MainHttpCall.create(testApi.test()).requestOnMainThread({
            tv_index.text = it.message
        }) {
            tv_index.text = it.city
        }
    }

    //**
    // flow
    fun testFlow() {
        lifecycleScope.launch {
            testApi.test().asFlow().catch {
                //异常
                tv_index.text = it.message
            }.collect {
                //成功处理
                tv_index.text = it.city
            }
        }
    }

    /**
     * 协程
     */
    fun testCoroutine() {
        lifecycleScope.launch {
            try {
                val cityInfo = testApi.test().getResult()
                tv_index.text = cityInfo.city
            } catch (e: Exception) {
                tv_index.text = e.message
            }
        }
    }

    /**
     * 协程2
     */
    fun testCoroutine2() {
        lifecycleScope.launch {
            //可不需要处理异常
            val cityInfo = testApi.test().getResultOrNull() { //处理异常
                tv_index.text = it.message
            }
            tv_index.text = cityInfo?.city
        }
    }


}