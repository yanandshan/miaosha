package com.qfedu.pojo;

/**
 * ResultEntry 是一个用于后台传递数据给前端的一个数据格式，一般用于ajax请求
 */
public class ResultEntry<T> {

    private boolean result;
    private T  data;


//    public ResultEntry(boolean result, T data) {
//        this.result = result;
//        this.data = data;
//
//    }

    public ResultEntry(boolean result, T data) {
        this.result = result;
        this.data = data;
    }



    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
