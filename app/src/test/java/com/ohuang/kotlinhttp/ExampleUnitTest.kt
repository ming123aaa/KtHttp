package com.ohuang.kotlinhttp

import com.ohuang.kthttp.call.await
import kotlinx.coroutines.runBlocking
import org.junit.Test
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

            var waitResult = testApi.getUrlContent("https://www.baidu.com/s?wd=contentLength%20%E5%A6%82%E6%9E%9C%E4%B8%8D%E7%9F%A5%E9%81%93%E5%8F%AF%E4%BB%A5%E5%A1%AB%E5%A4%9A%E5%B0%91&rsv_spt=1&rsv_iqid=0xfb597365000b97f6&issp=1&f=8&rsv_bp=1&rsv_idx=2&ie=utf-8&rqlang=cn&tn=baiduhome_pg&rsv_enter=1&rsv_dl=tb_enter&oq=InputStreamInputStream%2520%25E5%258F%25AF%25E4%25BB%25A5%25E6%258B%25B7%25E8%25B4%259D%25E5%2590%2597&rsv_btype=t&inputT=1332025&rsv_t=be0bjXqDLuz321SSzBHEfCLDlvM7422Yp1oS6z4kaFpoIS5u5%2BNVV%2FqI0WWSvYCSE9yX&rsv_sug3=44&rsv_sug1=14&rsv_sug7=100&rsv_n=2&rsv_pq=cef2e58b006c61cb&prefixsug=InputStreamInputStream%2520%2520%25E5%258F%25AF%25E4%25BB%25A5%25E6%258B%25B7%25E8%25B4%259D%25E5%2590%2597&rsp=5&rsv_sug4=1332025").await()


            println("waitResult=${waitResult}")
            println("time=${System.currentTimeMillis() - currentTimeMillis}")
        }

    }
}