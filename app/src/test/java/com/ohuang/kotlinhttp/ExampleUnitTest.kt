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

            var waitResult = testApi.download(File("D:\\ali213\\Download\\a.png"),"https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimage109.360doc.com%2FDownloadImg%2F2025%2F04%2F0321%2F296122601_4_20250403090445718&refer=http%3A%2F%2Fimage109.360doc.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1759655631&t=029ebfc1149170d60b65918d57bb0437", onProcess =  {p,t->
                println("p=$p,t=$t")
            }).await()


            println("waitResult=${waitResult}")
            println("time=${System.currentTimeMillis() - currentTimeMillis}")
        }

    }
}