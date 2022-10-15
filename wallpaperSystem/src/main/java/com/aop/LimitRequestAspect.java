package com.aop;

import com.exception.RequestFrequentlyExeception;
import com.util.IpUtil;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class LimitRequestAspect  {
    private static ConcurrentHashMap<String, ExpiringMap<String, Integer>> book = new ConcurrentHashMap<>();

    // 定义切点
    // 让所有有@LimitRequest注解的方法都执行切面方法
    @Pointcut(value = "@annotation(limitRequest)")
    public void excudeService(LimitRequest limitRequest){}

    //环绕通知
    @Around(value = "excudeService(limitRequest)")
    public Object doAround(ProceedingJoinPoint pjp, LimitRequest limitRequest) {
        // 获得request对象
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();
        HttpServletResponse response = sra.getResponse();
        //获取用户真实的ip
        String realIp = IpUtil.getIpAddr(request);
        // 获取Map对象， 如果没有则返回默认值
        // 第一个参数是key， 第二个参数是默认值
        ExpiringMap<String, Integer> uc = book.getOrDefault(request.getRequestURI(), ExpiringMap.builder().variableExpiration().build());
        //获取该IP访问方法的时间
        Integer uCount = uc.getOrDefault(realIp, 0);
        if (uCount >= limitRequest.count()) { // 超过次数，不执行目标方法
            //设置响应码
            response.setStatus(403);
            throw new RequestFrequentlyExeception("请求频繁:"+realIp);
        } else if (uCount == 0){
            // 第一次请求时，设置有效时间
            uc.put(realIp, uCount + 1, ExpirationPolicy.CREATED, limitRequest.time(), TimeUnit.MILLISECONDS);
        } else {
            // 未超过次数， 记录加一
            uc.put(realIp, uCount + 1);
        }
        book.put(request.getRequestURI(), uc);
        // result的值就是被拦截方法的返回值
        Object result = null;
        try {
            result = pjp.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return result;

    }
}
