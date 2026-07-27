package com.ohuang.kotlinhttp;

import com.ohuang.kotlinhttp.data.CityInfo;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Test {

    public void test(){

        testApi.INSTANCE.demo().request()
                .onSuccess(result -> {})
                .onError(error->{});
    }
}
