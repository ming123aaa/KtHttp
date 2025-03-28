package com.ohuang.kotlinhttp

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.method.MovementMethod
import android.util.Log
import android.view.ActionMode
import android.view.ActionMode.*
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ohuang.kthttp.call.asFlow
import com.ohuang.kthttp.call.getResult
import com.ohuang.kthttp.call.getResultSafe
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File


class ViewActivity : AppCompatActivity() {
    private val TAG = "ViewActivity"
    var lastEvent: MotionEvent? = null

    val tv_index: TextView by lazy {
        findViewById<TextView>(R.id.tv_index)
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

        findViewById<View>(R.id.tv_button).setOnClickListener {
            testCoroutine2()
        }


    }

    /**
     * 测试正常请求
     * requestOnMainThread可在主线程请求
     */
    fun test(){
        testApi.test().requestOnMainThread({
            tv_index.text = it.message
        }){
            tv_index.text = it.city
        }
    }

    //**
    // flow
  fun testFlow(){
        lifecycleScope.launch {
            testApi.test().asFlow().catch {
                //异常
                tv_index.text = it.message
            }.collect{
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
                val cityInfo = testApi.test().getResultSafe(){ //处理异常
                    tv_index.text = it.message
                }
                tv_index.text = cityInfo?.city

        }
    }






}