package com.ohuang.kotlinhttp;

import com.ohuang.kotlinhttp.data.Post;
import com.ohuang.kthttp.call.ErrorResponseException;

/**
 * Java 调用 KtHttp 示例（全部使用公共测试服务器）
 */
public class Test {

    // === 通过 MainHttpCall 调用（推荐） ===

    public void callWithMainHttpCall() {
        testApi.INSTANCE.demoForJava()
                .request()
                .onSuccess(post -> System.out.println("Post " + post.getId() + ": " + post.getTitle()))
                .onError(throwable -> {
                    if (throwable instanceof ErrorResponseException) {
                        ErrorResponseException e = (ErrorResponseException) throwable;
                        System.out.println("HTTP " + e.code() + ": " + e.httpMessage());
                        System.out.println("Body: " + e.errorBodyString());
                    }
                });
    }

    // === 回调到主线程 ===

    public void callOnMainThread() {
        testApi.INSTANCE.demoForJava()
                .requestOnMainThread()
                .onSuccess(post -> System.out.println("Post: " + post.getTitle()))
                .onError(throwable -> System.out.println("错误: " + throwable.getMessage()));
    }

    // === 阻塞等待结果 ===

    public void callSync() {
        try {
            Post post = testApi.INSTANCE.demoForJava().waitResult(5000);
            System.out.println("Post: " + post.getTitle());
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    // === 等待结果，异常返回 null ===

    public void callSyncOrNull() {
        Post post = testApi.INSTANCE.demoForJava().waitResultOrNull(5000);
        if (post != null) {
            System.out.println("Post: " + post.getTitle());
        }
    }

    // === 直接通过 HttpCall 获取字符串数据 ===

    public void callStringDirect() {
        MainHttpCall<String> call = MainHttpCall.Companion.create(testApi.INSTANCE.getHttpBin());
        call.request()
                .onSuccess(result -> System.out.println("结果长度: " + result.length()))
                .onError(throwable -> System.out.println("错误: " + throwable.getMessage()));
    }

    // === 获取文件信息 ===

    public void getFileInfo() {
        MainHttpCall<?> call = MainHttpCall.Companion.create(
                testApi.INSTANCE.checkFileInfo("https://httpbin.org/bytes/1024")
        );
        call.request()
                .onSuccess(info -> {
                    com.ohuang.kthttp.download.FileInfo fileInfo =
                            (com.ohuang.kthttp.download.FileInfo) info;
                    System.out.println("大小: " + fileInfo.getContentLength() + " bytes");
                })
                .onError(throwable -> throwable.printStackTrace());
    }
}