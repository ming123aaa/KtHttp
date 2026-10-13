# KtHttp

基于 Kotlin + OkHttp 的轻量级 HTTP 客户端库，支持回调、协程和 Flow 多种响应处理方式。

| 特性 | 说明 |
|------|------|
| 请求方式 | GET / POST / PUT / DELETE / PATCH / HEAD |
| 响应处理 | 回调 / `suspend` 协程 / Flow |
| 数据解析 | 内置 Gson，可扩展自定义 Transform |
| 文件操作 | 断点下载、进度回调、文件上传 |
| 状态码控制 | 自定义哪些 HTTP 状态码视为成功 |

---

## 依赖配置

要求 Kotlin >= 2.0.0。

`settings.gradle.kts` 添加 JitPack 仓库：

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

模块 `build.gradle.kts` 添加依赖：

```kotlin
dependencies {
    implementation("com.github.ming123aaa:KtHttp:2.1.0")
}
```

---

## 快速开始

```kotlin
// 创建实例
val httpClient = KtHttp()

// 定义请求
fun getUser(): HttpCall<User> {
    return httpClient.jsonCall<User> {
        url("https://api.example.com/user")
        addHeaders {
            addParam("Authorization", "Bearer xxx")
        }
        url("https://api.example.com/user") {
            addParam("id", "123")
        }
    }
}

// ---- 发起请求（三选一） ----

// 1. 回调
getUser().request(
    error = { e -> println(e.message) },
    callback = { user -> println(user) }
)

// 2. 协程
lifecycleScope.launch {
    try {
        val user = getUser().await()
    } catch (e: Exception) {
        // 处理异常
    }
}

// 3. Flow
lifecycleScope.launch {
    getUser().asFlow()
        .catch { e -> /* 异常处理 */ }
        .collect { user -> /* 成功处理 */ }
}
```

---

## 核心 API

### HttpClient

```kotlin
class HttpClient(
    var okHttpClient: OkHttpClient = OkHttpClient(),           // 自定义 OkHttpClient（超时/拦截器等）
    private var globalKtConfigCall: KtHttpRequest.() -> Unit = {}, // 全局默认配置，不覆盖请求级配置
    private var forceKtConfigCall: KtHttpRequest.() -> Unit = {},  // 全局强制配置，会覆盖请求级配置
    private var deferRequestBuild: Boolean = true                   // 是否延迟构建 Request（默认开启）
)
```

### 请求方法

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `responseCall { }` | `HttpCall<Response>` | 获取原始 OkHttp Response |
| `stringCall { }` | `HttpCall<String>` | 获取响应字符串 |
| `jsonCall<T> { }` | `HttpCall<T>` | JSON 自动解析（Gson） |
| `httpCall(transform) { }` | `HttpCall<T>` | 自定义 Transform 解析 |
| `httpCall(convert) { }` | `HttpCall<T>` | 自定义 ResponseConvert 解析 |
| `httpResponseCall(transform) { }` | `HttpCall<HttpResponse<T>>` | 获取包装响应（含原始 Response + body） |
| `jsonHttpResponseCall<T> { }` | `HttpCall<HttpResponse<T>>` | JSON + 包装响应 |

### HttpCall 接口

```kotlin
interface HttpCall<T> {
    fun request(error: (Throwable) -> Unit = {}, callback: (T) -> Unit)
    fun getOkhttpCall(): Call
    fun cancel()
    fun isCancelled(): Boolean
    fun isExecuted(): Boolean
    fun getConfigs(): MutableMap<String, Any>
}
```

### 协程扩展

| 方法 | 说明 |
|------|------|
| `await(isCancel = true)` | 挂起获取结果，取消协程时自动取消请求 |
| `awaitOrNull(isCancel = true, block = {})` | 挂起获取结果，异常时返回 null |
| `asFlow(isCancel = true)` | 转为 Flow |
| `asFlowOrNull(isCancel = true)` | 转为 Flow，异常时 emit null |
| `waitResult(timeOut = 0)` | 阻塞当前线程等待结果（0 = 无限等待） |
| `waitResultOrNull(timeOut = 0, block = {})` | 阻塞等待，异常时返回 null |

### 数据转换

```kotlin
// map 转换
val call: HttpCall<String> = httpClient.jsonCall<User> { url("...") }
    .map { it.name }

// 自定义 Transform
class MyTransform : Transform<MyType> {
    override fun transform(string: String): MyType = ...
}
httpClient.httpCall(MyTransform()) { url("...") }

// 自定义 Gson 实例
gsonTransFormSetGson(GsonBuilder().setDateFormat("yyyy-MM-dd").create())
```

---

## HTTP 请求方法

```kotlin
httpClient.stringCall {
    url("https://api.example.com/resource")

    get()                        // GET
    post { addParam("k", "v") } // POST 表单
    postJson { addParam("k", "v") }       // POST JSON
    postJson("""{"k":"v"}""")             // POST JSON 字符串
    put(body)                   // PUT
    delete()                    // DELETE（无 body）
    delete(body)                // DELETE（带 body）
    patch(body)                 // PATCH
    head()                      // HEAD
}
```

---

## 请求配置

### 请求构建块

```kotlin
httpClient.stringCall {
    url("https://api.example.com/data")       // 设置 URL
    url("https://api.example.com/data") {     // 带 URL 参数
        addParam("page", "1")
    }
    addHeader("Content-Type", "application/json")
    addHeaders {
        addParam("Authorization", "Bearer xxx")
    }
    requestBuilderBlock { }  // 直接操作 OkHttp Request.Builder
}
```

### Hook / 监听

```kotlin
httpClient.stringCall {
    url("https://api.example.com/data")

    hookResponse { response ->           // 替换/修改 Response
        println("hook: $it")
        return@hookResponse it
    }
    hookStringBody { body ->             // 替换/修改响应体字符串
        return@hookStringBody body.replace("old", "new")
    }
    onResponse { response -> }           // 回调 Response
    onStringBody { body -> }             // 回调字符串响应体
    onError { throwable, call, response -> }  // 错误回调
    onRequest { call -> }                // 请求发起回调
    onError { throwable -> }             // 错误回调（简化版）
}
```

### 全局配置

```kotlin
val httpClient = HttpClient(
    globalKtConfigCall = {
        onStringBody { body -> println("全局日志: $body") }
    },
    forceKtConfigCall = {
        onError { e, call, response -> println("错误: ${call.request().url}") }
    }
)
```

---

## HTTP 状态码控制

默认 200-299 视为成功，非此范围抛 `ErrorResponseException`。可通过 `validHttpCode` 自定义：

```kotlin
// 自定义成功状态码范围（如 200-404 都视为成功）
httpClient.stringCall {
    url("https://api.example.com/data")
    validHttpCode { code -> code in 200..404 }
}

// 关闭自动抛异常（所有状态码都进入成功回调）
httpClient.stringCall {
    url("https://api.example.com/data")
    validHttpCode(null)
}
```

---

## 异常处理

```kotlin
try {
    val result = httpClient.jsonCall<Data> { url("...") }.await()
} catch (e: ErrorResponseException) {
    val code = e.code()                    // HTTP 状态码
    val message = e.httpMessage()          // HTTP 状态消息
    val body = e.errorBodyString()         // 错误响应体
    val headers = e.errorResponse.headers() // 错误响应头
    val rawResponse = e.errorResponse.raw() // 原始 Response
} catch (e: EmptyBodyException) {
    // 响应体为空
} catch (e: KtHttpException) {
    // 其他框架异常
}
```

---

## 文件下载

支持断点续传和进度回调：

```kotlin
// 下载文件
val call = httpClient.download(
    file = File("/path/to/save.apk"),
    isContinueDownload = true,  // 开启断点续传
    onProcess = { current, total ->
        println("进度: ${current}/${total}")
    }
) {
    url("https://example.com/file.apk")
}

// 获取文件信息（大小、文件名、类型）
httpClient.downloadFileInfo { url("https://example.com/file.apk") }
    .request(callback = { info ->
        println("大小: ${info.contentLength}, 文件名: ${info.fileName}")
    })

// 仅获取文件大小
httpClient.downloadFileSize { url("https://example.com/file.apk") }
    .request(callback = { size -> println("$size bytes") })
```

---

## 文件上传

```kotlin
httpClient.stringCall {
    url("https://api.example.com/upload")
    uploadFile {
        addFile(
            key = "file",
            file = File("/path/to/file.png"),
            fileName = "upload.png",
            callBack = { current, total ->
                println("上传进度: ${current}/${total}")
            }
        )
    }
}
```

---

## 重试

```kotlin
// 自动重试
lifecycleScope.launch {
    val result = httpClient.retryOnFailure(
        count = 3,    // 最多重试 3 次
        time = 1000   // 每次间隔 1 秒
    ) {
        jsonCall<Data> { url("https://api.example.com/data") }
    }
}

// 顶层重试函数
val result = httpCallRetryOnFailure(count = 3, time = 1000) {
    httpClient.jsonCall<Data> { url("https://api.example.com/data") }
}
```

---

## Java 调用

如需在 Java 中使用，通过 `MainHttpCall` 包装：

```kotlin
// Kotlin 侧
fun getUserMain(): MainHttpCall<User> = getUser().toMainHttpCall()
```

```java
// Java 侧
MainHttpCall<User> call = MainHttpCall.Companion.create(demo());
call.request()
    .onSuccess(user -> { /* 成功 */ })
    .onError(error -> { /* 失败 */ });
```

---

## 自定义 Transform

实现 `Transform<T>` 接口接入其他解析库（如 Moshi、kotlinx.serialization）：

```kotlin
class MoshiTransform<T>(private val adapter: JsonAdapter<T>) : Transform<T> {
    override fun transform(string: String): T = adapter.fromJson(string)!!
}

// 使用
val moshi = Moshi.Builder().build()
httpClient.httpCall(
    transform = MoshiTransform(moshi.adapter(User::class.java))
) {
    url("https://api.example.com/user")
}
```