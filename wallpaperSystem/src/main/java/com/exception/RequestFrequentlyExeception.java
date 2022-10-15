package com.exception;

//自定义异常类，当请求频繁时调用
public class RequestFrequentlyExeception extends RuntimeException{
    static final long serialVersionUID = -7034897590745766939L;
    public RequestFrequentlyExeception(){}
    public RequestFrequentlyExeception(String msg){
        super(msg);
    }
}
