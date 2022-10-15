package com.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j
@Component
@Aspect
public class TimeConsumeAspect {

    /**
     * 切点定义为注解@annotation(注解类路径)
     */
    @Pointcut("@annotation(com.util.TimeConsume)")
    public void consume(){
    }

    @Around("consume()")
    public  <T> T around(ProceedingJoinPoint pjp) throws Throwable {
        Long startTime = System.currentTimeMillis();

        Object[] args = pjp.getArgs();
        T result;
        Method methodClass;
        try {
            result = (T)pjp.proceed(args);//执行方法
        }finally {
            long endTime = System.currentTimeMillis();
            Signature signature = pjp.getSignature();
            String methodName = signature.getName();
            Class<?> targetClass = pjp.getTarget().getClass();
            Class[] parameterTypes = ((MethodSignature) pjp.getSignature()).getParameterTypes();
            methodClass = targetClass.getMethod(methodName, parameterTypes);
            Annotation[] annotations = methodClass.getAnnotations();
            for (Annotation annotation : annotations){
                Class<? extends Annotation> aClass = annotation.annotationType();
                String simpleName = aClass.getSimpleName();
                if("TimeConsume".equals(simpleName)){
                    TimeConsume timeConsume = (TimeConsume) annotation;
                    String value = timeConsume.value();
                    log.info(value+"[{}] 执行耗时：{}ms",methodName,endTime-startTime);
                    break;
                }
            }

        }

        return result;
    }
}


