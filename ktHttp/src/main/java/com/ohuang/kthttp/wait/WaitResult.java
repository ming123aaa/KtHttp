package com.ohuang.kthttp.wait;


public class WaitResult<T> {

    private final boolean isSuccess;
    private final T result;
    private final Throwable throwable;

    public boolean isSuccess() {
        return isSuccess;
    }

    public T getResult() {
        return result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public WaitResult(T result) {
        this.isSuccess = true;
        this.result = result;
        this.throwable = null;
    }

    public WaitResult(Throwable throwable) {
        this.isSuccess = false;
        this.result = null;
        this.throwable = throwable;
    }


}
