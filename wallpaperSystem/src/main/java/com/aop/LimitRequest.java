package com.aop;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME) //JVM运行时生效
public @interface LimitRequest {
    int time() default 5000; //单位是毫秒，限制固定时间内不可超出最大请求次数
    int count() default 4;  //请求次数
}
