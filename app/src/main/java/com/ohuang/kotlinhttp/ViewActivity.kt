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
import com.ohuang.kthttp.call.asFlow
import com.ohuang.kthttp.call.getResult
import com.ohuang.kthttp.call.getResultOrNull
import com.ohuang.kthttp.download.DownloadCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File


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
    var download:DownloadCall?=null

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
                if (download==null) {
                    tv_button.text = "停止"
                    download =
                        testApi.download(file = File(cacheDir.absolutePath + "/my.apk")) { current, total ->
                            stateFlow.value = "下载进度：${current * 100 / total}%"
                        }
                    var resultOrNull = download?.getResultOrNull()
                    if (resultOrNull != null) {
                        stateFlow.value = "下载完成"
                        tv_button.text = "开始"
                        download = null
                    }
                }else{
                    tv_button.text = "开始"
                    download?.cancel()
                    download = null
                }
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
        testApi.test().requestOnMainThread({
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