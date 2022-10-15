package com.config;

import com.exception.RequestFrequentlyExeception;
import com.util.DataInter;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/*
    Controller异常处理器,使用AOP机制
 */
@RestControllerAdvice
public class ControllerExceptionAdvice {
    @ExceptionHandler
    public DataInter<?> handleException(Exception e){
        e.printStackTrace();
        DataInter<?> dataInter = new DataInter<>();
        dataInter.setMsg("服务器出错，请检查一下代码哦-_-");
        dataInter.setCode(1);
        return dataInter;
    }

    /*
        请求频繁异常处理
     */
    @ExceptionHandler
    public DataInter<?> handleException(RequestFrequentlyExeception e){
        DataInter<?> dataInter = new DataInter<>();
        System.out.println(e.getMessage());
        dataInter.setMsg(e.getMessage());
        dataInter.setCode(1);
        return dataInter;
    }

}
