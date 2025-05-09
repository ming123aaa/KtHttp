package com.ohuang.kthttp.wait;

/**
 * 多线程等待传递数据
 */
public class ThreadWait<T> {


    private  final Object lock = new Object();
    private volatile WaitResult<T> result;

    public void setResult(WaitResult<T> result) {
        if (result == null) {
            throw new IllegalArgumentException("Result cannot be null");
        }
        synchronized (lock) {
            this.result = result;
            lock.notifyAll();
        }
    }


    public WaitResult<T> waitResult(long time) {
        if (result != null) {
            return result;
        }
        synchronized (lock) {
            try {
                lock.wait(time);
            } catch (InterruptedException ignored) {

            }
            return result;
        }
    }



}
