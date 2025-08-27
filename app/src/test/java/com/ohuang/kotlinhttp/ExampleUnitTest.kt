package com.ohuang.kotlinhttp

import com.ohuang.kthttp.call.asFlow
import com.ohuang.kthttp.call.await
import com.ohuang.kthttp.call.getResult
import com.ohuang.kthttp.call.getResultSafe
import com.ohuang.kthttp.call.waitResult
import com.ohuang.kthttp.call.waitResultOrNull
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Test

import org.junit.Assert.*
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        runBlocking {
            var currentTimeMillis = System.currentTimeMillis()

            var waitResult = testApi.getUrlContent("http://192.168.2.123:8080/file.html1").await()


            println("waitResult=${waitResult}")
            println("time=${System.currentTimeMillis() - currentTimeMillis}")
        }

    }
}